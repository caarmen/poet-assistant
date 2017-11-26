package ca.rmen.android.poetassistant

import java.util.StringTokenizer

object TtsSplitter {

    /**
     * Splits a string into multiple tokens for pausing playback.
     *
     * @param text A "..." in the input text indicates a pause, and each subsequent "." after the initial "..." indicates an additional pause.
     *
     * Examples:
     * "To be or not to be... that is the question":  1 pause:  "To be or not to be", "",     " that is the question"
     * "To be or not to be.... that is the question": 2 pauses: "To be or not to be", "", "", " that is the question"
     * "To be or not to be. that is the question":    0 pauses: "To be or not to be. that is the question"
     *
     * @return the input split into multiple tokens. An empty-string token in the result indicates a pause.
     */
    fun split(text: String): List<String> {
        val tokens = ArrayList<String>()
        val stringTokenizer = StringTokenizer(text, ".", true)
        // In a sequence of dots, we want to skip the first two.
        var skippedDots = 0
        var prevToken: String? = null
        while (stringTokenizer.hasMoreTokens()) {
            val token = stringTokenizer.nextToken()
            // The current token is a dot. It may or may not be used to pause.
            if ("." == token) {
                // We've skipped at least two consecutive dots. We can now start adding all dots as
                // pause tokens.
                if (skippedDots == 2) {
                    val pauseToken = ""
                    tokens.add(pauseToken)
                    prevToken = pauseToken
                }
                // Beginning of a dot sequence. We have to skip the first two dots.
                else {
                    skippedDots++
                }
            }
            // The current token is actual text to speak.
            else {
                var textToken: String
                // This is either the first text token of the entire input, or a text token after a pause token.
                // We simply add it to the list.
                if (prevToken == null || "" == prevToken) {
                    textToken = token
                    tokens.add(textToken)
                }
                // The previous token was also actual text.
                // Concatenate the previous token with this one, separating by a single period.
                // This optimization allows us to minimize the number of tokens we'll return, and to rely
                // on the sentence pausing of the TTS engine when less than 3 dots separate two sentences.
                else /* prevToken != null && prevToken != "" */ {
                    textToken = prevToken + "." + token
                    tokens[tokens.size - 1] = textToken
                }
                prevToken = textToken
                skippedDots = 0

            }
        }
        return tokens
    }
}