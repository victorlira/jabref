package org.jabref.logic.citationkeypattern;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

import org.jabref.model.FieldChange;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.strings.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the utility class of the LabelPattern package.
 * It provides functionality to generate citation keys based on user-defined patterns.
 */
public class CitationKeyGenerator extends BracketedPattern {
    /**
     * All single characters that we can use for extending a key to make it unique.
     */
    public static final String APPENDIX_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    /**
     * List of unwanted characters. These will be removed at the end.
     * Note that <code>+</code> is a wanted character to indicate "et al." in authorsAlpha.
     */
    public static final String DEFAULT_UNWANTED_CHARACTERS = "-`ʹ:!;?^";

    private static final Logger LOGGER = LoggerFactory.getLogger(CitationKeyGenerator.class);

    // Source of disallowed characters: https://tex.stackexchange.com/a/408548/9075
    private static final List<Character> DISALLOWED_CHARACTERS = Arrays.asList('{', '}', '(', ')', ',', '=', '\\', '"', '#', '%', '~', '\'');

    private final AbstractCitationKeyPatterns citeKeyPattern;
    private final BibDatabase database;
    private final CitationKeyPatternPreferences citationKeyPatternPreferences;
    private final String unwantedCharacters;

    public CitationKeyGenerator(BibDatabaseContext bibDatabaseContext, CitationKeyPatternPreferences citationKeyPatternPreferences) {
        this(bibDatabaseContext.getMetaData().getCiteKeyPatterns(citationKeyPatternPreferences.getKeyPatterns()),
                bibDatabaseContext.getDatabase(),
                citationKeyPatternPreferences);
    }

    public CitationKeyGenerator(AbstractCitationKeyPatterns citeKeyPattern, BibDatabase database, CitationKeyPatternPreferences citationKeyPatternPreferences) {
        this.citeKeyPattern = Objects.requireNonNull(citeKeyPattern);
        this.database = Objects.requireNonNull(database);
        this.citationKeyPatternPreferences = Objects.requireNonNull(citationKeyPatternPreferences);
        this.unwantedCharacters = citationKeyPatternPreferences.getUnwantedCharacters();
    }

    /**
     * Computes an appendix to a citation key that could make it unique. We use a-z for numbers 0-25, and then aa-az, ba-bz, etc.
     *
     * @param number The appendix number.
     * @return The String to append.
     */
    private static String getAppendix(int number) {
        if (number >= APPENDIX_CHARACTERS.length()) {
            int lastChar = number % APPENDIX_CHARACTERS.length();
            return getAppendix((number / APPENDIX_CHARACTERS.length()) - 1) + APPENDIX_CHARACTERS.charAt(lastChar);
        } else {
            return APPENDIX_CHARACTERS.substring(number, number + 1);
        }
    }

    /**
     * Removes default unwanted characters from the given key.
     *
     * @param key the citation key
     * @return the cleaned key
     */
    public static String removeDefaultUnwantedCharacters(String key) {
        return removeUnwantedCharacters(key, DEFAULT_UNWANTED_CHARACTERS);
    }

    /**
     * Removes unwanted characters from the given key.
     *
     * @param key the citation key
     * @param unwantedCharacters characters to be removed
     * @return the cleaned key
     */
    public static String removeUnwantedCharacters(String key, String unwantedCharacters) {
        String newKey = key.chars()
                .filter(c -> unwantedCharacters.indexOf(c) == -1)
                .filter(c -> !DISALLOWED_CHARACTERS.contains((char) c))
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        // Replace non-English characters like umlauts etc. with a sensible
        // letter or letter combination that bibtex can accept.
        return StringUtil.replaceSpecialCharacters(newKey);
    }

    /**
     * Cleans the citation key by removing unwanted characters and spaces.
     *
     * @param key the citation key
     * @param unwantedCharacters characters to be removed
     * @return the cleaned key
     */
    public static String cleanKey(String key, String unwantedCharacters) {
        return removeUnwantedCharacters(key, unwantedCharacters).replaceAll("\\s", "");
    }

    /**
     * Generate a citation key for the given {@link BibEntry}.
     *
     * @param entry a {@link BibEntry}
     * @return a citation key based on the user's preferences
     */
    public String generateKey(BibEntry entry) {
        Objects.requireNonNull(entry);
        String currentKey = entry.getCitationKey().orElse(null);

        String newKey = createCitationKeyFromPattern(entry);
        newKey = replaceWithRegex(newKey);
        newKey = appendLettersToKey(newKey, currentKey);
        return cleanKey(newKey, unwantedCharacters);
    }

    /**
     * A letter will be appended to the key based on the user's preferences, either always or to prevent duplicated keys.
     *
     * @param key    the new key
     * @param oldKey the old key
     * @return a key, if needed, with an appended letter
     */
    private String appendLettersToKey(String key, String oldKey) {
        long occurrences = database.getNumberOfCitationKeyOccurrences(key);

        if ((occurrences > 0) && Objects.equals(oldKey, key)) {
            occurrences--; // No change, so we can accept one dupe.
        }

        boolean alwaysAddLetter = citationKeyPatternPreferences.getKeySuffix()
                == CitationKeyPatternPreferences.KeySuffix.ALWAYS;

        if (alwaysAddLetter || occurrences != 0) {
            // The key is already in use, so we must modify it.
            boolean firstLetterA = citationKeyPatternPreferences.getKeySuffix()
                    == CitationKeyPatternPreferences.KeySuffix.SECOND_WITH_A;

            int number = !alwaysAddLetter && !firstLetterA ? 1 : 0;
            String moddedKey;

            do {
                moddedKey = key + getAppendix(number);
                number++;

                occurrences = database.getNumberOfCitationKeyOccurrences(moddedKey);
                // only happens if #getAddition() is buggy
                if (Objects.equals(oldKey, moddedKey)) {
                    occurrences--;
                }
            } while (occurrences > 0);
            return moddedKey;
        }
        return key;
    }

    private String createCitationKeyFromPattern(BibEntry entry) {
        String patternString = citeKeyPattern.getPattern(entry);
        return formatPattern(patternString, entry);
    }

    private String replaceWithRegex(String key) {
        try {
            return key.replaceAll(citationKeyPatternPreferences.getKeyPatternRegex(), citationKeyPatternPreferences.getKeyPatternReplacement());
        } catch (PatternSyntaxException e) {
            LOGGER.error("Invalid regex pattern provided.", e);
            return key;
        }
    }

    private String formatPattern(String pattern, BibEntry entry) {
        // Expand the brackets in the pattern
        return expandBrackets(pattern, expandBracketContent(entry));
    }

    /**
     * A helper method to create a {@link Function} that takes a single bracketed expression, expands it, and cleans the key.
     *
     * @param entry the {@link BibEntry} that a citation key is generated for
     * @return a cleaned citation key for the given {@link BibEntry}
     */
    private Function<String, String> expandBracketContent(BibEntry entry) {
        Character keywordDelimiter = citationKeyPatternPreferences.getKeywordDelimiter();

        return (String bracket) -> {
            String expandedPattern;
            List<String> fieldParts = parseFieldAndModifiers(bracket);

            expandedPattern = removeUnwantedCharacters(getFieldValue(entry, fieldParts.get(0), keywordDelimiter, database), unwantedCharacters);
            // check whether there is a modifier on the end such as ":lower":
            if (fieldParts.size() > 1) {
                // apply modifiers:
                expandedPattern = applyModifiers(expandedPattern, fieldParts, 1, expandBracketContent(entry));
            }
            return cleanKey(expandedPattern, unwantedCharacters);
        };
    }

    static String applyModifiers(String value, List<String> fieldParts, int startIndex, Function<String, String> expandBracketContent) {
        for (int i = startIndex; i < fieldParts.size(); i++) {
            String modifier = fieldParts.get(i).toLowerCase();

            switch (modifier) {
                case "veryshorttitle":
                    value = extractFirstSignificantWord(value);
                    break;
                case "lower":
                    value = value.toLowerCase();
                    break;
                case "upper":
                    value = value.toUpperCase();
                    break;
                case "capitalize":
                    value = capitalize(value);
                    break;
                case "sentencecase":
                    value = sentenceCase(value);
                    break;
                case "titlecase":
                    value = titleCase(value);
                    break;
                case "abbr":
                    value = abbreviate(value);
                    break;
                default:
                    if (modifier.startsWith("truncate")) {
                        int length = Integer.parseInt(modifier.replace("truncate", ""));
                        value = truncateValue(value, length);
                    } else if (modifier.startsWith("regex")) {
                        // Handle regex modifier
                    }
                    break;
            }
        }
        return value;
    }

    private static String extractFirstSignificantWord(String value) {
        String[] words = value.split("\\s+");
        for (String word : words) {
            if (!isFunctionWord(word)) {
                return word;
            }
        }
        return value;
    }

    private static boolean isFunctionWord(String word) {
        List<String> functionWords = Arrays.asList("the", "with", "and", "or", "but");
        return functionWords.contains(word.toLowerCase());
    }

    private static String truncateValue(String value, int length) {
        return value.length() > length ? value.substring(0, length) : value;
    }

    private static String abbreviate(String value) {
        String[] words = value.split(" ");
        StringBuilder abbr = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                abbr.append(word.charAt(0));
            }
        }
        return abbr.toString();
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String[] words = value.split("\\s+");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            capitalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
        }
        return capitalized.toString().trim();
    }

    private static String sentenceCase(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String[] words = value.split("\\s+");
        StringBuilder sentenceCase = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                sentenceCase.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1).toLowerCase());
            } else {
                sentenceCase.append(words[i].toLowerCase());
            }
            if (i < words.length - 1) {
                sentenceCase.append(" ");
            }
        }
        return sentenceCase.toString();
    }

    private static String titleCase(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String[] words = value.split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (titleCase.length() > 0) {
                titleCase.append(" ");
            }
            titleCase.append(capitalize(word));
        }
        return titleCase.toString();
    }

    public Optional<FieldChange> generateAndSetKey(BibEntry entry) {
        String newKey = generateKey(entry);
        return entry.setCitationKey(newKey);
    }
}
