// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class container for currently-supported Culture Models in Confirm and Choice
 * Prompt.
 */
public final class PromptCultureModels {

    private PromptCultureModels() {

    }

    public static final String BULGARIAN_CULTURE = "bg-bg";
    public static final String CHINESE_CULTURE = "zh-cn";
    public static final String DUTCH_CULTURE = "nl-nl";
    public static final String ENGLISH_CULTURE = "en-us";
    public static final String FRENCH_CULTURE = "fr-fr";
    public static final String GERMAN_CULTURE = "de-de";
    public static final String HINDI_CULTURE = "hi-in";
    public static final String ITALIAN_CULTURE = "it-it";
    public static final String JAPANESE_CULTURE = "ja-jp";
    public static final String KOREAN_CULTURE = "ko-kr";
    public static final String PORTUGUESE_CULTURE = "pt-br";
    public static final String SPANISH_CULTURE = "es-es";
    public static final String SWEDISH_CULTURE = "sv-se";
    public static final String TURKISH_CULTURE = "tr-tr";

    /**
     * Gets the bulgarian prompt culture model.
     */
    public static final PromptCultureModel BULGARIAN = new PromptCultureModel() {
        {
            setInlineOr(" или ");
            setInlineOrMore(", или ");
            setLocale(BULGARIAN_CULTURE);
            setNoInLanguage("Не");
            setSeparator(", ");
            setYesInLanguage("да");
        }
    };

    public static final PromptCultureModel CHINESE = new PromptCultureModel() {
        {
            setInlineOr(" 要么 ");
            setInlineOrMore("， 要么 ");
            setLocale(CHINESE_CULTURE);
            setNoInLanguage("不");
            setSeparator("， ");
            setYesInLanguage("是的");
        }
    };

    /**
     * Gets the dutch prompt culture model.
     */
    public static final PromptCultureModel DUTCH = new PromptCultureModel() {
        {
            setInlineOr(" of ");
            setInlineOrMore(", of ");
            setLocale(DUTCH_CULTURE);
            setNoInLanguage("Nee");
            setSeparator(", ");
            setYesInLanguage("Ja");
        }
    };

    /**
     * Gets the english prompt culture model.
     */
    public static final PromptCultureModel ENGLISH = new PromptCultureModel() {
        {
            setInlineOr(" or ");
            setInlineOrMore(", or ");
            setLocale(ENGLISH_CULTURE);
            setNoInLanguage("No");
            setSeparator(", ");
            setYesInLanguage("Yes");
        }
    };

    /**
     * Gets the french prompt culture model.
     */
    public static final PromptCultureModel FRENCH = new PromptCultureModel() {
        {
            setInlineOr(" ou ");
            setInlineOrMore(", ou ");
            setLocale(FRENCH_CULTURE);
            setNoInLanguage("Non");
            setSeparator(", ");
            setYesInLanguage("Oui");
        }
    };

    /**
     * Gets the german prompt culture model.
     */
    public static final PromptCultureModel GERMAN = new PromptCultureModel() {
        {
            setInlineOr(" oder ");
            setInlineOrMore(", oder ");
            setLocale(GERMAN_CULTURE);
            setNoInLanguage("Nein");
            setSeparator(", ");
            setYesInLanguage("Ja");
        }
    };

    /**
     * Gets the hindi prompt culture model.
     */
    public static final PromptCultureModel HINDI = new PromptCultureModel() {
        {
            setInlineOr(" या ");
            setInlineOrMore(", या ");
            setLocale(HINDI_CULTURE);
            setNoInLanguage("नहीं");
            setSeparator(", ");
            setYesInLanguage("हां");
        }
    };

    /**
     * Gets the italian prompt culture model.
     */
    public static final PromptCultureModel ITALIAN = new PromptCultureModel() {
        {
            setInlineOr(" o ");
            setInlineOrMore(" o ");
            setLocale(ITALIAN_CULTURE);
            setNoInLanguage("No");
            setSeparator(", ");
            setYesInLanguage("Si");
        }
    };

    /**
     * Gets the japanese prompt culture model.
     */
    public static final PromptCultureModel JAPANESE = new PromptCultureModel() {
        {
            setInlineOr(" または ");
            setInlineOrMore("、 または ");
            setLocale(JAPANESE_CULTURE);
            setNoInLanguage("いいえ");
            setSeparator("、 ");
            setYesInLanguage("はい");
        }
    };

    /**
     * Gets the korean prompt culture model.
     */
    public static final PromptCultureModel KOREAN = new PromptCultureModel() {
        {
            setInlineOr(" 또는 ");
            setInlineOrMore(" 또는 ");
            setLocale(KOREAN_CULTURE);
            setNoInLanguage("아니");
            setSeparator(", ");
            setYesInLanguage("예");
        }
    };

    /**
     * Gets the portuguese prompt culture model.
     */
    public static final PromptCultureModel PORTUGUESE = new PromptCultureModel() {
        {
            setInlineOr(" ou ");
            setInlineOrMore(", ou ");
            setLocale(PORTUGUESE_CULTURE);
            setNoInLanguage("Não");
            setSeparator(", ");
            setYesInLanguage("Sim");
        }
    };

    /**
     * Gets the spanish prompt culture model.
     */
    public static final PromptCultureModel SPANISH = new PromptCultureModel() {
        {
            setInlineOr(" o ");
            setInlineOrMore(", o ");
            setLocale(SPANISH_CULTURE);
            setNoInLanguage("No");
            setSeparator(", ");
            setYesInLanguage("Sí");
        }
    };

    /**
     * Gets the swedish prompt culture model.
     */
    public static final PromptCultureModel SWEDISH = new PromptCultureModel() {
        {
            setInlineOr(" eller ");
            setInlineOrMore(" eller ");
            setLocale(SWEDISH_CULTURE);
            setNoInLanguage("Nej");
            setSeparator(", ");
            setYesInLanguage("Ja");
        }
    };

    /**
     * Gets the turkish prompt culture model.
     */
    public static final PromptCultureModel TURKISH = new PromptCultureModel() {
        {
            setInlineOr(" veya ");
            setInlineOrMore(" veya ");
            setLocale(TURKISH_CULTURE);
            setNoInLanguage("Hayır");
            setSeparator(", ");
            setYesInLanguage("Evet");
        }
    };

    private static PromptCultureModel[] promptCultureModelArray =
    {
        BULGARIAN,
        CHINESE,
        DUTCH,
        ENGLISH,
        FRENCH,
        GERMAN,
        HINDI,
        ITALIAN,
        JAPANESE,
        KOREAN,
        PORTUGUESE,
        SPANISH,
        SWEDISH,
        TURKISH
    };

    /**
     * Gets a list of the supported culture models.
     *
     * @return   Array of {@link PromptCultureModel} with the supported cultures.
     */
    public static PromptCultureModel[] getSupportedCultures() {
        return promptCultureModelArray;
    }

    private static final List<String> SUPPORTED_LOCALES = Arrays.stream(getSupportedCultures())
                                                               .map(x -> x.getLocale()).collect(Collectors.toList());

    // private static List<String> supportedlocales;

    // static {
    //     supportedlocales = new ArrayList<String>();
    //     PromptCultureModel[] cultures = getSupportedCultures();
    //     for (PromptCultureModel promptCultureModel : cultures) {
    //         supportedlocales.add(promptCultureModel.getLocale());
    //     }
    // }

    /**
     * Use Recognizers-Text to normalize various potential setLocale strings to a standard.
     *
     * This is mostly a copy/paste from
     * https://github.com/microsoft/Recognizers-Text/blob/master/.NET/Microsoft.Recognizers.Text/C
     * lture.cs#L66 This doesn't directly use Recognizers-Text's MapToNearestLanguage because if
     * they add language support before we do, it will break our prompts.
     *
     * @param cultureCode  Represents setLocale. Examples: "en-US, en-us, EN".
     *
     * @return   Normalized setLocale.
     */
    public static String mapToNearestLanguage(String cultureCode) {
        cultureCode = cultureCode.toLowerCase();
        final String cCode = cultureCode;

        if (SUPPORTED_LOCALES.stream().allMatch(o -> o != cCode)) {
            // Handle cases like EnglishOthers with cultureCode "en-*"
            List<String> fallbackCultureCodes = SUPPORTED_LOCALES.stream()
                                                        .filter(o -> o.endsWith("*")
                                                                 && cCode.startsWith(o.split("-")[0]))
                                                        .collect(Collectors.toList());

            if (fallbackCultureCodes.size() == 1) {
                return fallbackCultureCodes.get(0);
            }

            //If there is no cultureCode like "-*", map only the prefix
            //For example, "es-mx" will be mapped to "es-es"
            fallbackCultureCodes = SUPPORTED_LOCALES.stream()
                                                   .filter(o -> cCode.startsWith(o.split("-")[0]))
                                                   .collect(Collectors.toList());

            if (fallbackCultureCodes.size() > 0) {
                return fallbackCultureCodes.get(0);
            }
        }

        return cultureCode;
    }
}
