/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.index.fulltext.languages;

import org.dizitart.no2.index.fulltext.Language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Slovak stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Slovak implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "a",
                "aby",
                "aj",
                "ak",
                "ako",
                "aký",
                "ale",
                "alebo",
                "and",
                "ani",
                "asi",
                "avšak",
                "až",
                "ba",
                "bez",
                "bol",
                "bola",
                "boli",
                "bolo",
                "bude",
                "budem",
                "budeme",
                "budete",
                "budeš",
                "budú",
                "buï",
                "buď",
                "by",
                "byť",
                "cez",
                "dnes",
                "do",
                "ešte",
                "for",
                "ho",
                "hoci",
                "i",
                "iba",
                "ich",
                "im",
                "iné",
                "iný",
                "ja",
                "je",
                "jeho",
                "jej",
                "jemu",
                "ju",
                "k",
                "kam",
                "každá",
                "každé",
                "každí",
                "každý",
                "kde",
                "kedže",
                "keï",
                "keď",
                "kto",
                "ktorou",
                "ktorá",
                "ktoré",
                "ktorí",
                "ktorý",
                "ku",
                "lebo",
                "len",
                "ma",
                "mať",
                "medzi",
                "menej",
                "mi",
                "mna",
                "mne",
                "mnou",
                "moja",
                "moje",
                "mu",
                "musieť",
                "my",
                "má",
                "máte",
                "mòa",
                "môcť",
                "môj",
                "môže",
                "na",
                "nad",
                "nami",
                "naši",
                "nech",
                "neho",
                "nej",
                "nemu",
                "než",
                "nich",
                "nie",
                "niektorý",
                "nielen",
                "nim",
                "nič",
                "no",
                "nová",
                "nové",
                "noví",
                "nový",
                "nám",
                "nás",
                "náš",
                "ním",
                "o",
                "od",
                "odo",
                "of",
                "on",
                "ona",
                "oni",
                "ono",
                "ony",
                "po",
                "pod",
                "podľa",
                "pokiaľ",
                "potom",
                "pre",
                "pred",
                "predo",
                "preto",
                "pretože",
                "prečo",
                "pri",
                "prvá",
                "prvé",
                "prví",
                "prvý",
                "práve",
                "pýta",
                "s",
                "sa",
                "seba",
                "sem",
                "si",
                "sme",
                "so",
                "som",
                "späť",
                "ste",
                "svoj",
                "svoje",
                "svojich",
                "svojím",
                "svojími",
                "sú",
                "ta",
                "tak",
                "taký",
                "takže",
                "tam",
                "te",
                "teba",
                "tebe",
                "tebou",
                "teda",
                "tej",
                "ten",
                "tento",
                "the",
                "ti",
                "tie",
                "tieto",
                "tiež",
                "to",
                "toho",
                "tohoto",
                "tom",
                "tomto",
                "tomu",
                "tomuto",
                "toto",
                "tou",
                "tu",
                "tvoj",
                "tvojími",
                "ty",
                "tá",
                "táto",
                "tú",
                "túto",
                "tým",
                "týmto",
                "tě",
                "už",
                "v",
                "vami",
                "vaše",
                "veï",
                "viac",
                "vo",
                "vy",
                "vám",
                "vás",
                "váš",
                "však",
                "všetok",
                "z",
                "za",
                "zo",
                "a",
                "áno",
                "èi",
                "èo",
                "èí",
                "òom",
                "òou",
                "òu",
                "či",
                "čo",
                "ďalšia",
                "ďalšie",
                "ďalší",
                "že"
        ));
    }
}
