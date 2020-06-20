/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.index.fulltext.languages;

import org.dizitart.no2.index.fulltext.Language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hungarian stop words
 *
 * @author Anindya Chatterjee
 * @since 2.1.0
 */
public class Hungarian implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
            "a",
            "abba",
            "abban",
            "abbã³l",
            "abból",
            "addig",
            "ahhoz",
            "ahogy",
            "ahol",
            "aki",
            "akik",
            "akkor",
            "akár",
            "akã¡r",
            "alapján",
            "alapjã¡n",
            "alatt",
            "alatta",
            "alattad",
            "alattam",
            "alattatok",
            "alattuk",
            "alattunk",
            "alá",
            "alád",
            "alájuk",
            "alám",
            "alánk",
            "alátok",
            "alã¡",
            "alã¡d",
            "alã¡juk",
            "alã¡m",
            "alã¡nk",
            "alã¡tok",
            "alã³l",
            "alã³la",
            "alã³lad",
            "alã³lam",
            "alã³latok",
            "alã³luk",
            "alã³lunk",
            "alól",
            "alóla",
            "alólad",
            "alólam",
            "alólatok",
            "alóluk",
            "alólunk",
            "amely",
            "amelybol",
            "amelyek",
            "amelyekben",
            "amelyeket",
            "amelyet",
            "amelyik",
            "amelynek",
            "ami",
            "amikor",
            "amit",
            "amolyan",
            "amott",
            "amã­g",
            "amíg",
            "annak",
            "annál",
            "annã¡l",
            "arra",
            "arrã³l",
            "arról",
            "attã³l",
            "attól",
            "az",
            "aznap",
            "azok",
            "azokat",
            "azokba",
            "azokban",
            "azokbã³l",
            "azokból",
            "azokhoz",
            "azokig",
            "azokkal",
            "azokká",
            "azokkã¡",
            "azoknak",
            "azoknál",
            "azoknã¡l",
            "azokon",
            "azokra",
            "azokrã³l",
            "azokról",
            "azoktã³l",
            "azoktól",
            "azokã©rt",
            "azokért",
            "azon",
            "azonban",
            "azonnal",
            "azt",
            "aztán",
            "aztã¡n",
            "azután",
            "azzal",
            "azzá",
            "azzã¡",
            "azã©rt",
            "azért",
            "bal",
            "balra",
            "ban",
            "be",
            "belã©",
            "belã©d",
            "belã©jã¼k",
            "belã©m",
            "belã©nk",
            "belã©tek",
            "belã¼l",
            "belå‘le",
            "belå‘led",
            "belå‘lem",
            "belå‘letek",
            "belå‘lã¼k",
            "belå‘lã¼nk",
            "belé",
            "beléd",
            "beléjük",
            "belém",
            "belénk",
            "belétek",
            "belül",
            "belőle",
            "belőled",
            "belőlem",
            "belőletek",
            "belőlük",
            "belőlünk",
            "ben",
            "benne",
            "benned",
            "bennem",
            "bennetek",
            "bennã¼k",
            "bennã¼nk",
            "bennük",
            "bennünk",
            "bár",
            "bárcsak",
            "bármilyen",
            "bã¡r",
            "bã¡rcsak",
            "bã¡rmilyen",
            "bãºcsãº",
            "búcsú",
            "cikk",
            "cikkek",
            "cikkeket",
            "csak",
            "csakhogy",
            "csupán",
            "csupã¡n",
            "de",
            "dehogy",
            "e",
            "ebbe",
            "ebben",
            "ebbå‘l",
            "ebből",
            "eddig",
            "egy",
            "egyebek",
            "egyebet",
            "egyedã¼l",
            "egyedül",
            "egyelå‘re",
            "egyelőre",
            "egyes",
            "egyet",
            "egyetlen",
            "egyik",
            "egymás",
            "egymã¡s",
            "egyre",
            "egyszerre",
            "egyã©b",
            "egyã¼tt",
            "egyéb",
            "együtt",
            "egã©sz",
            "egã©szen",
            "egész",
            "egészen",
            "ehhez",
            "ekkor",
            "el",
            "eleinte",
            "ellen",
            "ellenes",
            "elleni",
            "ellenã©re",
            "ellenére",
            "elmondta",
            "elså‘",
            "elså‘k",
            "elså‘sorban",
            "elså‘t",
            "elsõ",
            "első",
            "elsők",
            "elsősorban",
            "elsőt",
            "elã©",
            "elã©d",
            "elã©g",
            "elã©jã¼k",
            "elã©m",
            "elã©nk",
            "elã©tek",
            "elå‘bb",
            "elå‘l",
            "elå‘le",
            "elå‘led",
            "elå‘lem",
            "elå‘letek",
            "elå‘lã¼k",
            "elå‘lã¼nk",
            "elå‘szã¶r",
            "elå‘tt",
            "elå‘tte",
            "elå‘tted",
            "elå‘ttem",
            "elå‘ttetek",
            "elå‘ttã¼k",
            "elå‘ttã¼nk",
            "elå‘zå‘",
            "elé",
            "eléd",
            "elég",
            "eléjük",
            "elém",
            "elénk",
            "elétek",
            "elõ",
            "elõször",
            "elõtt",
            "elő",
            "előbb",
            "elől",
            "előle",
            "előled",
            "előlem",
            "előletek",
            "előlük",
            "előlünk",
            "először",
            "előtt",
            "előtte",
            "előtted",
            "előttem",
            "előttetek",
            "előttük",
            "előttünk",
            "előző",
            "emilyen",
            "engem",
            "ennek",
            "ennyi",
            "ennã©l",
            "ennél",
            "enyã©m",
            "enyém",
            "erre",
            "errå‘l",
            "erről",
            "esetben",
            "ettå‘l",
            "ettől",
            "ez",
            "ezek",
            "ezekbe",
            "ezekben",
            "ezekbå‘l",
            "ezekből",
            "ezeken",
            "ezeket",
            "ezekhez",
            "ezekig",
            "ezekkel",
            "ezekkã©",
            "ezekké",
            "ezeknek",
            "ezeknã©l",
            "ezeknél",
            "ezekre",
            "ezekrå‘l",
            "ezekről",
            "ezektå‘l",
            "ezektől",
            "ezekã©rt",
            "ezekért",
            "ezen",
            "ezentãºl",
            "ezentúl",
            "ezer",
            "ezret",
            "ezt",
            "ezután",
            "ezutã¡n",
            "ezzel",
            "ezzã©",
            "ezzé",
            "ezã©rt",
            "ezért",
            "fel",
            "fele",
            "felek",
            "felet",
            "felett",
            "felã©",
            "felé",
            "fent",
            "fenti",
            "fã©l",
            "fã¶lã©",
            "fél",
            "fölé",
            "gyakran",
            "ha",
            "hallã³",
            "halló",
            "hamar",
            "hanem",
            "harmadik",
            "harmadikat",
            "harminc",
            "hat",
            "hatodik",
            "hatodikat",
            "hatot",
            "hatvan",
            "helyett",
            "hetedik",
            "hetediket",
            "hetet",
            "hetven",
            "hirtelen",
            "hiszen",
            "hiába",
            "hiã¡ba",
            "hogy",
            "hogyan",
            "hol",
            "holnap",
            "holnapot",
            "honnan",
            "hova",
            "hozzá",
            "hozzád",
            "hozzájuk",
            "hozzám",
            "hozzánk",
            "hozzátok",
            "hozzã¡",
            "hozzã¡d",
            "hozzã¡juk",
            "hozzã¡m",
            "hozzã¡nk",
            "hozzã¡tok",
            "hurrá",
            "hurrã¡",
            "huszadik",
            "hány",
            "hányszor",
            "hármat",
            "három",
            "hát",
            "hátha",
            "hátulsó",
            "hã¡ny",
            "hã¡nyszor",
            "hã¡rmat",
            "hã¡rom",
            "hã¡t",
            "hã¡tha",
            "hã¡tulsã³",
            "hã©t",
            "hãºsz",
            "hét",
            "húsz",
            "ide",
            "ide-ð¾da",
            "ide-оda",
            "idã©n",
            "idén",
            "igazán",
            "igazã¡n",
            "igen",
            "ill",
            "ill.",
            "illetve",
            "ilyen",
            "ilyenkor",
            "immár",
            "immã¡r",
            "inkább",
            "inkã¡bb",
            "is",
            "ismã©t",
            "ismét",
            "ison",
            "itt",
            "jelenleg",
            "jobban",
            "jobbra",
            "jã³",
            "jã³l",
            "jã³lesik",
            "jã³val",
            "jã¶vå‘re",
            "jó",
            "jól",
            "jólesik",
            "jóval",
            "jövőre",
            "kell",
            "kellene",
            "kellett",
            "kelljen",
            "keressünk",
            "keresztül",
            "ketten",
            "kettå‘",
            "kettå‘t",
            "kettő",
            "kettőt",
            "kevã©s",
            "kevés",
            "ki",
            "kiben",
            "kibå‘l",
            "kiből",
            "kicsit",
            "kicsoda",
            "kihez",
            "kik",
            "kikbe",
            "kikben",
            "kikbå‘l",
            "kikből",
            "kiken",
            "kiket",
            "kikhez",
            "kikkel",
            "kikkã©",
            "kikké",
            "kiknek",
            "kiknã©l",
            "kiknél",
            "kikre",
            "kikrå‘l",
            "kikről",
            "kiktå‘l",
            "kiktől",
            "kikã©rt",
            "kikért",
            "kilenc",
            "kilencedik",
            "kilencediket",
            "kilencet",
            "kilencven",
            "kin",
            "kinek",
            "kinã©l",
            "kinél",
            "kire",
            "kirå‘l",
            "kiről",
            "kit",
            "kitå‘l",
            "kitől",
            "kivel",
            "kivã©",
            "kivé",
            "kiã©",
            "kiã©rt",
            "kié",
            "kiért",
            "korábban",
            "korã¡bban",
            "kã©pest",
            "kã©rem",
            "kã©rlek",
            "kã©sz",
            "kã©så‘",
            "kã©så‘bb",
            "kã©så‘n",
            "kã©t",
            "kã©tszer",
            "kã¶rã¼l",
            "kã¶szã¶nhetå‘en",
            "kã¶szã¶nã¶m",
            "kã¶zben",
            "kã¶zel",
            "kã¶zepesen",
            "kã¶zepã©n",
            "kã¶zã©",
            "kã¶zã¶tt",
            "kã¶zã¼l",
            "kã¼lã¶n",
            "kã¼lã¶nben",
            "kã¼lã¶nbã¶zå‘",
            "kã¼lã¶nbã¶zå‘bb",
            "kã¼lã¶nbã¶zå‘ek",
            "képest",
            "kérem",
            "kérlek",
            "kész",
            "késő",
            "később",
            "későn",
            "két",
            "kétszer",
            "kívül",
            "körül",
            "köszönhetően",
            "köszönöm",
            "közben",
            "közel",
            "közepesen",
            "közepén",
            "közé",
            "között",
            "közül",
            "külön",
            "különben",
            "különböző",
            "különbözőbb",
            "különbözőek",
            "lassan",
            "le",
            "legalább",
            "legalã¡bb",
            "legyen",
            "lehet",
            "lehetetlen",
            "lehetett",
            "lehetå‘leg",
            "lehetå‘sã©g",
            "lehetőleg",
            "lehetőség",
            "lenne",
            "lenni",
            "lennã©k",
            "lennã©nek",
            "lennék",
            "lennének",
            "lesz",
            "leszek",
            "lesznek",
            "leszã¼nk",
            "leszünk",
            "lett",
            "lettek",
            "lettem",
            "lettã¼nk",
            "lettünk",
            "lã©vå‘",
            "lévő",
            "ma",
            "maga",
            "magad",
            "magam",
            "magatokat",
            "magukat",
            "magunkat",
            "magát",
            "magã¡t",
            "mai",
            "majd",
            "majdnem",
            "manapság",
            "manapsã¡g",
            "meg",
            "megcsinál",
            "megcsinálnak",
            "megcsinã¡l",
            "megcsinã¡lnak",
            "megint",
            "megvan",
            "mellett",
            "mellette",
            "melletted",
            "mellettem",
            "mellettetek",
            "mellettã¼k",
            "mellettã¼nk",
            "mellettük",
            "mellettünk",
            "mellã©",
            "mellã©d",
            "mellã©jã¼k",
            "mellã©m",
            "mellã©nk",
            "mellã©tek",
            "mellå‘l",
            "mellå‘le",
            "mellå‘led",
            "mellå‘lem",
            "mellå‘letek",
            "mellå‘lã¼k",
            "mellå‘lã¼nk",
            "mellé",
            "melléd",
            "melléjük",
            "mellém",
            "mellénk",
            "mellétek",
            "mellől",
            "mellőle",
            "mellőled",
            "mellőlem",
            "mellőletek",
            "mellőlük",
            "mellőlünk",
            "mely",
            "melyek",
            "melyik",
            "mennyi",
            "mert",
            "mi",
            "miatt",
            "miatta",
            "miattad",
            "miattam",
            "miattatok",
            "miattuk",
            "miattunk",
            "mibe",
            "miben",
            "mibå‘l",
            "miből",
            "mihez",
            "mik",
            "mikbe",
            "mikben",
            "mikbå‘l",
            "mikből",
            "miken",
            "miket",
            "mikhez",
            "mikkel",
            "mikkã©",
            "mikké",
            "miknek",
            "miknã©l",
            "miknél",
            "mikor",
            "mikre",
            "mikrå‘l",
            "mikről",
            "miktå‘l",
            "miktől",
            "mikã©rt",
            "mikért",
            "milyen",
            "min",
            "mind",
            "mindegyik",
            "mindegyiket",
            "minden",
            "mindenesetre",
            "mindenki",
            "mindent",
            "mindenã¼tt",
            "mindenütt",
            "mindig",
            "mindketten",
            "minek",
            "minket",
            "mint",
            "mintha",
            "minã©l",
            "minél",
            "mire",
            "mirå‘l",
            "miről",
            "mit",
            "mitå‘l",
            "mitől",
            "mivel",
            "mivã©",
            "mivé",
            "miã©rt",
            "miért",
            "mondta",
            "most",
            "mostanáig",
            "mostanã¡ig",
            "már",
            "más",
            "másik",
            "másikat",
            "másnap",
            "második",
            "másodszor",
            "mások",
            "másokat",
            "mást",
            "mã¡r",
            "mã¡s",
            "mã¡sik",
            "mã¡sikat",
            "mã¡snap",
            "mã¡sodik",
            "mã¡sodszor",
            "mã¡sok",
            "mã¡sokat",
            "mã¡st",
            "mã©g",
            "mã©gis",
            "mã­g",
            "mã¶gã©",
            "mã¶gã©d",
            "mã¶gã©jã¼k",
            "mã¶gã©m",
            "mã¶gã©nk",
            "mã¶gã©tek",
            "mã¶gã¶tt",
            "mã¶gã¶tte",
            "mã¶gã¶tted",
            "mã¶gã¶ttem",
            "mã¶gã¶ttetek",
            "mã¶gã¶ttã¼k",
            "mã¶gã¶ttã¼nk",
            "mã¶gã¼l",
            "mã¶gã¼le",
            "mã¶gã¼led",
            "mã¶gã¼lem",
            "mã¶gã¼letek",
            "mã¶gã¼lã¼k",
            "mã¶gã¼lã¼nk",
            "mãºltkor",
            "mãºlva",
            "még",
            "mégis",
            "míg",
            "mögé",
            "mögéd",
            "mögéjük",
            "mögém",
            "mögénk",
            "mögétek",
            "mögött",
            "mögötte",
            "mögötted",
            "mögöttem",
            "mögöttetek",
            "mögöttük",
            "mögöttünk",
            "mögül",
            "mögüle",
            "mögüled",
            "mögülem",
            "mögületek",
            "mögülük",
            "mögülünk",
            "múltkor",
            "múlva",
            "na",
            "nagy",
            "nagyobb",
            "nagyon",
            "naponta",
            "napot",
            "ne",
            "negyedik",
            "negyediket",
            "negyven",
            "neked",
            "nekem",
            "neki",
            "nekik",
            "nektek",
            "nekã¼nk",
            "nekünk",
            "nem",
            "nemcsak",
            "nemrã©g",
            "nemrég",
            "nincs",
            "nyolc",
            "nyolcadik",
            "nyolcadikat",
            "nyolcat",
            "nyolcvan",
            "nála",
            "nálad",
            "nálam",
            "nálatok",
            "náluk",
            "nálunk",
            "nã¡la",
            "nã¡lad",
            "nã¡lam",
            "nã¡latok",
            "nã¡luk",
            "nã¡lunk",
            "nã©gy",
            "nã©gyet",
            "nã©ha",
            "nã©hã¡ny",
            "nã©lkã¼l",
            "négy",
            "négyet",
            "néha",
            "néhány",
            "nélkül",
            "o",
            "oda",
            "ok",
            "olyan",
            "onnan",
            "ott",
            "pedig",
            "persze",
            "pár",
            "pã¡r",
            "pã©ldã¡ul",
            "például",
            "rajta",
            "rajtad",
            "rajtam",
            "rajtatok",
            "rajtuk",
            "rajtunk",
            "rendben",
            "rosszul",
            "rá",
            "rád",
            "rájuk",
            "rám",
            "ránk",
            "rátok",
            "rã¡",
            "rã¡d",
            "rã¡juk",
            "rã¡m",
            "rã¡nk",
            "rã¡tok",
            "rã©gen",
            "rã©gã³ta",
            "rã©szã©re",
            "rã³la",
            "rã³lad",
            "rã³lam",
            "rã³latok",
            "rã³luk",
            "rã³lunk",
            "rã¶gtã¶n",
            "régen",
            "régóta",
            "részére",
            "róla",
            "rólad",
            "rólam",
            "rólatok",
            "róluk",
            "rólunk",
            "rögtön",
            "s",
            "saját",
            "se",
            "sem",
            "semmi",
            "semmilyen",
            "semmisã©g",
            "semmiség",
            "senki",
            "soha",
            "sok",
            "sokan",
            "sokat",
            "sokkal",
            "sokszor",
            "sokáig",
            "sokã¡ig",
            "során",
            "sorã¡n",
            "stb.",
            "szemben",
            "szerbusz",
            "szerint",
            "szerinte",
            "szerinted",
            "szerintem",
            "szerintetek",
            "szerintã¼k",
            "szerintã¼nk",
            "szerintük",
            "szerintünk",
            "szervusz",
            "szinte",
            "számára",
            "száz",
            "századik",
            "százat",
            "szã¡mã¡ra",
            "szã¡z",
            "szã¡zadik",
            "szã¡zat",
            "szã©pen",
            "szã­ves",
            "szã­vesen",
            "szã­veskedjã©k",
            "szépen",
            "szét",
            "szíves",
            "szívesen",
            "szíveskedjék",
            "så‘t",
            "sőt",
            "talán",
            "talã¡n",
            "tavaly",
            "te",
            "tegnap",
            "tegnapelå‘tt",
            "tegnapelőtt",
            "tehát",
            "tehã¡t",
            "tele",
            "teljes",
            "tessã©k",
            "tessék",
            "ti",
            "tied",
            "titeket",
            "tizedik",
            "tizediket",
            "tizenegy",
            "tizenegyedik",
            "tizenhat",
            "tizenhárom",
            "tizenhã¡rom",
            "tizenhã©t",
            "tizenhét",
            "tizenkettedik",
            "tizenkettå‘",
            "tizenkettő",
            "tizenkilenc",
            "tizenkã©t",
            "tizenkét",
            "tizennyolc",
            "tizennã©gy",
            "tizennégy",
            "tizenã¶t",
            "tizenöt",
            "tizet",
            "tovább",
            "további",
            "továbbá",
            "tovã¡bb",
            "tovã¡bbi",
            "távol",
            "tã¡vol",
            "tã©ged",
            "tã©nyleg",
            "tã­z",
            "tã¶bb",
            "tã¶bbi",
            "tã¶bbszã¶r",
            "tãºl",
            "tå‘le",
            "tå‘led",
            "tå‘lem",
            "tå‘letek",
            "tå‘lã¼k",
            "tå‘lã¼nk",
            "téged",
            "tényleg",
            "tíz",
            "több",
            "többi",
            "többször",
            "túl",
            "tőle",
            "tőled",
            "tőlem",
            "tőletek",
            "tőlük",
            "tőlünk",
            "ugyanakkor",
            "ugyanez",
            "ugyanis",
            "ugye",
            "urak",
            "uram",
            "urat",
            "utoljára",
            "utoljã¡ra",
            "utolsã³",
            "utolsó",
            "után",
            "utána",
            "utã¡n",
            "vagy",
            "vagyis",
            "vagyok",
            "vagytok",
            "vagyunk",
            "vajon",
            "valahol",
            "valaki",
            "valakit",
            "valamelyik",
            "valami",
            "valamint",
            "való",
            "van",
            "vannak",
            "vele",
            "veled",
            "velem",
            "veletek",
            "velã¼k",
            "velã¼nk",
            "velük",
            "velünk",
            "vissza",
            "viszlát",
            "viszlã¡t",
            "viszont",
            "viszontlátásra",
            "viszontlã¡tã¡sra",
            "volna",
            "volnának",
            "volnã¡nak",
            "volnã©k",
            "volnék",
            "volt",
            "voltak",
            "voltam",
            "voltunk",
            "vã©gre",
            "vã©gã©n",
            "vã©gã¼l",
            "végre",
            "végén",
            "végül",
            "által",
            "általában",
            "ám",
            "át",
            "ã¡ltal",
            "ã¡ltalã¡ban",
            "ã¡m",
            "ã¡t",
            "ã©ljen",
            "ã©n",
            "ã©rte",
            "ã©rted",
            "ã©rtem",
            "ã©rtetek",
            "ã©rtã¼k",
            "ã©rtã¼nk",
            "ã©s",
            "ã©v",
            "ã©vben",
            "ã©ve",
            "ã©vek",
            "ã©ves",
            "ã©vi",
            "ã©vvel",
            "ã­gy",
            "ã³ta",
            "ã¶n",
            "ã¶nbe",
            "ã¶nben",
            "ã¶nbå‘l",
            "ã¶nhã¶z",
            "ã¶nnek",
            "ã¶nnel",
            "ã¶nnã©l",
            "ã¶nre",
            "ã¶nrå‘l",
            "ã¶nt",
            "ã¶ntå‘l",
            "ã¶nã©rt",
            "ã¶nã¶k",
            "ã¶nã¶kbe",
            "ã¶nã¶kben",
            "ã¶nã¶kbå‘l",
            "ã¶nã¶ket",
            "ã¶nã¶khã¶z",
            "ã¶nã¶kkel",
            "ã¶nã¶knek",
            "ã¶nã¶knã©l",
            "ã¶nã¶kre",
            "ã¶nã¶krå‘l",
            "ã¶nã¶ktå‘l",
            "ã¶nã¶kã©rt",
            "ã¶nã¶kã¶n",
            "ã¶nã¶n",
            "ã¶t",
            "ã¶tven",
            "ã¶tã¶dik",
            "ã¶tã¶diket",
            "ã¶tã¶t",
            "ãºgy",
            "ãºgyis",
            "ãºgynevezett",
            "ãºjra",
            "ãºr",
            "å‘",
            "å‘k",
            "å‘ket",
            "å‘t",
            "éljen",
            "én",
            "éppen",
            "érte",
            "érted",
            "értem",
            "értetek",
            "értük",
            "értünk",
            "és",
            "év",
            "évben",
            "éve",
            "évek",
            "éves",
            "évi",
            "évvel",
            "így",
            "óta",
            "õ",
            "õk",
            "õket",
            "ön",
            "önbe",
            "önben",
            "önből",
            "önhöz",
            "önnek",
            "önnel",
            "önnél",
            "önre",
            "önről",
            "önt",
            "öntől",
            "önért",
            "önök",
            "önökbe",
            "önökben",
            "önökből",
            "önöket",
            "önökhöz",
            "önökkel",
            "önöknek",
            "önöknél",
            "önökre",
            "önökről",
            "önöktől",
            "önökért",
            "önökön",
            "önön",
            "össze",
            "öt",
            "ötven",
            "ötödik",
            "ötödiket",
            "ötöt",
            "úgy",
            "úgyis",
            "úgynevezett",
            "új",
            "újabb",
            "újra",
            "úr",
            "ő",
            "ők",
            "őket",
            "őt"
        ));
    }
}
