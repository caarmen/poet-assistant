/*
 * Copyright (c) 2016 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.dictionaries;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class TestPorterStemmer {

    PorterStemmer porterStemmer;

    @Before
    public void setup() {
        porterStemmer = new PorterStemmer();
    }

    @Test
    public void test_getM() {
        assertGetM("tr", 0);
        assertGetM("ee", 0);
        assertGetM("tree", 0);
        assertGetM("y", 0);
        assertGetM("by", 0);
        assertGetM("trouble", 1);
        assertGetM("oats", 1);
        assertGetM("trees", 1);
        assertGetM("ivy", 1);
        assertGetM("troubles", 2);
        assertGetM("private", 2);
        assertGetM("oaten", 2);
        assertGetM("orrery", 2);
    }

    @Test
    public void test_step1a() {
        assertStep1a("caresses", "caress");
        assertStep1a("ponies", "poni");
        assertStep1a("ties", "ti");
        assertStep1a("caress", "caress");
        assertStep1a("cats", "cat");
    }

    @Test
    public void test_step1b() {
        assertStep1b("feed", "feed");
        assertStep1b("agreed", "agree");
        assertStep1b("plastered", "plaster");
        assertStep1b("bled", "bled");
        assertStep1b("motoring", "motor");
        assertStep1b("sing", "sing");
        assertStep1b("conflated", "conflate");
        assertStep1b("troubled", "trouble");
        assertStep1b("sized", "size");
        assertStep1b("hopping", "hop");
        assertStep1b("tanned", "tan");
        assertStep1b("falling", "fall");
        assertStep1b("hissing", "hiss");
        assertStep1b("fizzed", "fizz");
        assertStep1b("failing", "fail");
        assertStep1b("filing", "file");
    }

    @Test
    public void test_step1c() {
        assertStep1c("happy", "happi");
        assertStep1c("sky", "sky");
    }

    @Test
    public void test_step2() {
        assertStep2("relational", "relate");
        assertStep2("conditional", "condition");
        assertStep2("rational", "rational");
        assertStep2("valenci", "valence");
        assertStep2("hesitanci", "hesitance");
        assertStep2("digitizer", "digitize");
        assertStep2("conformabli", "conformable");
        assertStep2("radicalli", "radical");
        assertStep2("differentli", "different");
        assertStep2("vileli", "vile");
        assertStep2("analogousli", "analogous");
        assertStep2("vietnamization", "vietnamize");
        assertStep2("predication", "predicate");
        assertStep2("operator", "operate");
        assertStep2("feudalism", "feudal");
        assertStep2("decisiveness", "decisive");
        assertStep2("hopefulness", "hopeful");
        assertStep2("callousness", "callous");
        assertStep2("formaliti", "formal");
        assertStep2("sensitiviti", "sensitive");
        assertStep2("sensibiliti", "sensible");
    }

    @Test
    public void test_step3() {
        assertStep3("triplicate","triplic");
        assertStep3("formative","form");
        assertStep3("formalize","formal");
        assertStep3("electriciti","electric");
        assertStep3("electrical","electric");
        assertStep3("hopeful","hope");
        assertStep3("goodness","good");
    }

    @Test
    public void test_step4() {
        assertStep4("revival","reviv");
        assertStep4("allowance","allow");
        assertStep4("inference","infer");
        assertStep4("airliner","airlin");
        assertStep4("gyroscopic","gyroscop");
        assertStep4("adjustable","adjust");
        assertStep4("defensible","defens");
        assertStep4("irritant","irrit");
        assertStep4("replacement","replac");
        assertStep4("adjustment","adjust");
        assertStep4("dependent","depend");
        assertStep4("adoption","adopt");
        assertStep4("homologou","homolog");
        assertStep4("communism","commun");
        assertStep4("activate","activ");
        assertStep4("angulariti","angular");
        assertStep4("homologous","homolog");
        assertStep4("effective","effect");
        assertStep4("bowdlerize","bowdler");
    }

    @Test
    public void test_step5a() {
        assertStep5a("probate", "probat");
        assertStep5a("rate", "rate");
        assertStep5a("cease", "ceas");
    }

    @Test
    public void test_step5b() {
        assertStep5b("controll", "control");
        assertStep5b("roll", "roll");
    }

    @Test
    public void testStemFiles() throws IOException {
        BufferedReader inputReader = new BufferedReader(new FileReader("src/test/resources/stemmer/input.txt"));
        BufferedReader expectedOutputReader = new BufferedReader(new FileReader("src/test/resources/stemmer/output.txt"));
        StringBuilder failedStems = new StringBuilder();

        for(String input = inputReader.readLine(); input != null; input= inputReader.readLine()) {
            String expectedStem = expectedOutputReader.readLine();
            String actualStem = porterStemmer.stemWord(input);
            if (!expectedStem.equals(actualStem)) {
                failedStems.append(String.format("%s -> %s instead of %s\n", input, actualStem, expectedStem));
            }
        }

        inputReader.close();
        expectedOutputReader.close();

        if (failedStems.length() > 0) {
            Assert.fail("Stem failures: " + failedStems.toString());
        }
    }

    private void assertGetM(String word, int expectedM) {
        String letterTypes = porterStemmer.getLetterTypes(word);
        int actualM = porterStemmer.getM(letterTypes);
        Assert.assertEquals(String.format(Locale.US, "Expected %s for word %s but got %d", word, expectedM, actualM), expectedM, actualM);
    }

    private void assertStep1a(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep1a(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 1a, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep1b(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep1b(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 1b, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep1c(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep1c(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 1c, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep2(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep2(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 2, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep3(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep3(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 3, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep4(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep4(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 4, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep5a(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep5a(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 5a, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }

    private void assertStep5b(String input, String expectedOutput) {
        String actualOutput = porterStemmer.stemStep5b(input);
        Assert.assertEquals(String.format(Locale.US, "Expected %s -> %s for step 5b, but got %s", input, expectedOutput, actualOutput), expectedOutput, actualOutput);
    }
}
