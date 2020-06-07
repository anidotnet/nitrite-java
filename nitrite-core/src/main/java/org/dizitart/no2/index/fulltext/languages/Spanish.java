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
 * Spanish stop words
 *
 * @author Anindya Chatterjee
 * @since 2.1.0
 */
public class Spanish implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "_",
            "a",
            "actualmente",
            "acuerdo",
            "adelante",
            "ademas",
            "además",
            "adrede",
            "afirmó",
            "agregó",
            "ahi",
            "ahora",
            "ahí",
            "al",
            "algo",
            "alguna",
            "algunas",
            "alguno",
            "algunos",
            "algún",
            "alli",
            "allí",
            "alrededor",
            "ambos",
            "ampleamos",
            "antano",
            "antaño",
            "ante",
            "anterior",
            "antes",
            "apenas",
            "aproximadamente",
            "aquel",
            "aquella",
            "aquellas",
            "aquello",
            "aquellos",
            "aqui",
            "aquél",
            "aquélla",
            "aquéllas",
            "aquéllos",
            "aquí",
            "arriba",
            "arribaabajo",
            "aseguró",
            "asi",
            "así",
            "atras",
            "aun",
            "aunque",
            "ayer",
            "añadió",
            "aún",
            "b",
            "bajo",
            "bastante",
            "bien",
            "breve",
            "buen",
            "buena",
            "buenas",
            "bueno",
            "buenos",
            "c",
            "cada",
            "casi",
            "cerca",
            "cierta",
            "ciertas",
            "cierto",
            "ciertos",
            "cinco",
            "claro",
            "comentó",
            "como",
            "con",
            "conmigo",
            "conocer",
            "conseguimos",
            "conseguir",
            "considera",
            "consideró",
            "consigo",
            "consigue",
            "consiguen",
            "consigues",
            "contigo",
            "contra",
            "cosas",
            "creo",
            "cual",
            "cuales",
            "cualquier",
            "cuando",
            "cuanta",
            "cuantas",
            "cuanto",
            "cuantos",
            "cuatro",
            "cuenta",
            "cuál",
            "cuáles",
            "cuándo",
            "cuánta",
            "cuántas",
            "cuánto",
            "cuántos",
            "cómo",
            "d",
            "da",
            "dado",
            "dan",
            "dar",
            "de",
            "debajo",
            "debe",
            "deben",
            "debido",
            "decir",
            "dejó",
            "del",
            "delante",
            "demasiado",
            "demás",
            "dentro",
            "deprisa",
            "desde",
            "despacio",
            "despues",
            "después",
            "detras",
            "detrás",
            "dia",
            "dias",
            "dice",
            "dicen",
            "dicho",
            "dieron",
            "diferente",
            "diferentes",
            "dijeron",
            "dijo",
            "dio",
            "donde",
            "dos",
            "durante",
            "día",
            "días",
            "dónde",
            "e",
            "ejemplo",
            "el",
            "ella",
            "ellas",
            "ello",
            "ellos",
            "embargo",
            "empleais",
            "emplean",
            "emplear",
            "empleas",
            "empleo",
            "en",
            "encima",
            "encuentra",
            "enfrente",
            "enseguida",
            "entonces",
            "entre",
            "era",
            "erais",
            "eramos",
            "eran",
            "eras",
            "eres",
            "es",
            "esa",
            "esas",
            "ese",
            "eso",
            "esos",
            "esta",
            "estaba",
            "estabais",
            "estaban",
            "estabas",
            "estad",
            "estada",
            "estadas",
            "estado",
            "estados",
            "estais",
            "estamos",
            "estan",
            "estando",
            "estar",
            "estaremos",
            "estará",
            "estarán",
            "estarás",
            "estaré",
            "estaréis",
            "estaría",
            "estaríais",
            "estaríamos",
            "estarían",
            "estarías",
            "estas",
            "este",
            "estemos",
            "esto",
            "estos",
            "estoy",
            "estuve",
            "estuviera",
            "estuvierais",
            "estuvieran",
            "estuvieras",
            "estuvieron",
            "estuviese",
            "estuvieseis",
            "estuviesen",
            "estuvieses",
            "estuvimos",
            "estuviste",
            "estuvisteis",
            "estuviéramos",
            "estuviésemos",
            "estuvo",
            "está",
            "estábamos",
            "estáis",
            "están",
            "estás",
            "esté",
            "estéis",
            "estén",
            "estés",
            "ex",
            "excepto",
            "existe",
            "existen",
            "explicó",
            "expresó",
            "f",
            "fin",
            "final",
            "fue",
            "fuera",
            "fuerais",
            "fueran",
            "fueras",
            "fueron",
            "fuese",
            "fueseis",
            "fuesen",
            "fueses",
            "fui",
            "fuimos",
            "fuiste",
            "fuisteis",
            "fuéramos",
            "fuésemos",
            "g",
            "general",
            "gran",
            "grandes",
            "gueno",
            "h",
            "ha",
            "haber",
            "habia",
            "habida",
            "habidas",
            "habido",
            "habidos",
            "habiendo",
            "habla",
            "hablan",
            "habremos",
            "habrá",
            "habrán",
            "habrás",
            "habré",
            "habréis",
            "habría",
            "habríais",
            "habríamos",
            "habrían",
            "habrías",
            "habéis",
            "había",
            "habíais",
            "habíamos",
            "habían",
            "habías",
            "hace",
            "haceis",
            "hacemos",
            "hacen",
            "hacer",
            "hacerlo",
            "haces",
            "hacia",
            "haciendo",
            "hago",
            "han",
            "has",
            "hasta",
            "hay",
            "haya",
            "hayamos",
            "hayan",
            "hayas",
            "hayáis",
            "he",
            "hecho",
            "hemos",
            "hicieron",
            "hizo",
            "horas",
            "hoy",
            "hube",
            "hubiera",
            "hubierais",
            "hubieran",
            "hubieras",
            "hubieron",
            "hubiese",
            "hubieseis",
            "hubiesen",
            "hubieses",
            "hubimos",
            "hubiste",
            "hubisteis",
            "hubiéramos",
            "hubiésemos",
            "hubo",
            "i",
            "igual",
            "incluso",
            "indicó",
            "informo",
            "informó",
            "intenta",
            "intentais",
            "intentamos",
            "intentan",
            "intentar",
            "intentas",
            "intento",
            "ir",
            "j",
            "junto",
            "k",
            "l",
            "la",
            "lado",
            "largo",
            "las",
            "le",
            "lejos",
            "les",
            "llegó",
            "lleva",
            "llevar",
            "lo",
            "los",
            "luego",
            "lugar",
            "m",
            "mal",
            "manera",
            "manifestó",
            "mas",
            "mayor",
            "me",
            "mediante",
            "medio",
            "mejor",
            "mencionó",
            "menos",
            "menudo",
            "mi",
            "mia",
            "mias",
            "mientras",
            "mio",
            "mios",
            "mis",
            "misma",
            "mismas",
            "mismo",
            "mismos",
            "modo",
            "momento",
            "mucha",
            "muchas",
            "mucho",
            "muchos",
            "muy",
            "más",
            "mí",
            "mía",
            "mías",
            "mío",
            "míos",
            "n",
            "nada",
            "nadie",
            "ni",
            "ninguna",
            "ningunas",
            "ninguno",
            "ningunos",
            "ningún",
            "no",
            "nos",
            "nosotras",
            "nosotros",
            "nuestra",
            "nuestras",
            "nuestro",
            "nuestros",
            "nueva",
            "nuevas",
            "nuevo",
            "nuevos",
            "nunca",
            "o",
            "ocho",
            "os",
            "otra",
            "otras",
            "otro",
            "otros",
            "p",
            "pais",
            "para",
            "parece",
            "parte",
            "partir",
            "pasada",
            "pasado",
            "paìs",
            "peor",
            "pero",
            "pesar",
            "poca",
            "pocas",
            "poco",
            "pocos",
            "podeis",
            "podemos",
            "poder",
            "podria",
            "podriais",
            "podriamos",
            "podrian",
            "podrias",
            "podrá",
            "podrán",
            "podría",
            "podrían",
            "poner",
            "por",
            "por qué",
            "porque",
            "posible",
            "primer",
            "primera",
            "primero",
            "primeros",
            "principalmente",
            "pronto",
            "propia",
            "propias",
            "propio",
            "propios",
            "proximo",
            "próximo",
            "próximos",
            "pudo",
            "pueda",
            "puede",
            "pueden",
            "puedo",
            "pues",
            "q",
            "qeu",
            "que",
            "quedó",
            "queremos",
            "quien",
            "quienes",
            "quiere",
            "quiza",
            "quizas",
            "quizá",
            "quizás",
            "quién",
            "quiénes",
            "qué",
            "r",
            "raras",
            "realizado",
            "realizar",
            "realizó",
            "repente",
            "respecto",
            "s",
            "sabe",
            "sabeis",
            "sabemos",
            "saben",
            "saber",
            "sabes",
            "sal",
            "salvo",
            "se",
            "sea",
            "seamos",
            "sean",
            "seas",
            "segun",
            "segunda",
            "segundo",
            "según",
            "seis",
            "ser",
            "sera",
            "seremos",
            "será",
            "serán",
            "serás",
            "seré",
            "seréis",
            "sería",
            "seríais",
            "seríamos",
            "serían",
            "serías",
            "seáis",
            "señaló",
            "si",
            "sido",
            "siempre",
            "siendo",
            "siete",
            "sigue",
            "siguiente",
            "sin",
            "sino",
            "sobre",
            "sois",
            "sola",
            "solamente",
            "solas",
            "solo",
            "solos",
            "somos",
            "son",
            "soy",
            "soyos",
            "su",
            "supuesto",
            "sus",
            "suya",
            "suyas",
            "suyo",
            "suyos",
            "sé",
            "sí",
            "sólo",
            "t",
            "tal",
            "tambien",
            "también",
            "tampoco",
            "tan",
            "tanto",
            "tarde",
            "te",
            "temprano",
            "tendremos",
            "tendrá",
            "tendrán",
            "tendrás",
            "tendré",
            "tendréis",
            "tendría",
            "tendríais",
            "tendríamos",
            "tendrían",
            "tendrías",
            "tened",
            "teneis",
            "tenemos",
            "tener",
            "tenga",
            "tengamos",
            "tengan",
            "tengas",
            "tengo",
            "tengáis",
            "tenida",
            "tenidas",
            "tenido",
            "tenidos",
            "teniendo",
            "tenéis",
            "tenía",
            "teníais",
            "teníamos",
            "tenían",
            "tenías",
            "tercera",
            "ti",
            "tiempo",
            "tiene",
            "tienen",
            "tienes",
            "toda",
            "todas",
            "todavia",
            "todavía",
            "todo",
            "todos",
            "total",
            "trabaja",
            "trabajais",
            "trabajamos",
            "trabajan",
            "trabajar",
            "trabajas",
            "trabajo",
            "tras",
            "trata",
            "través",
            "tres",
            "tu",
            "tus",
            "tuve",
            "tuviera",
            "tuvierais",
            "tuvieran",
            "tuvieras",
            "tuvieron",
            "tuviese",
            "tuvieseis",
            "tuviesen",
            "tuvieses",
            "tuvimos",
            "tuviste",
            "tuvisteis",
            "tuviéramos",
            "tuviésemos",
            "tuvo",
            "tuya",
            "tuyas",
            "tuyo",
            "tuyos",
            "tú",
            "u",
            "ultimo",
            "un",
            "una",
            "unas",
            "uno",
            "unos",
            "usa",
            "usais",
            "usamos",
            "usan",
            "usar",
            "usas",
            "uso",
            "usted",
            "ustedes",
            "v",
            "va",
            "vais",
            "valor",
            "vamos",
            "van",
            "varias",
            "varios",
            "vaya",
            "veces",
            "ver",
            "verdad",
            "verdadera",
            "verdadero",
            "vez",
            "vosotras",
            "vosotros",
            "voy",
            "vuestra",
            "vuestras",
            "vuestro",
            "vuestros",
            "w",
            "x",
            "y",
            "ya",
            "yo",
            "z",
            "él",
            "éramos",
            "ésa",
            "ésas",
            "ése",
            "ésos",
            "ésta",
            "éstas",
            "éste",
            "éstos",
            "última",
            "últimas",
            "último",
            "últimos"
        ));
    }
}
