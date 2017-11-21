package com.energizedwork.gradle.webdriver

import spock.lang.Specification
import spock.lang.Unroll

class StringUtilsSpec extends Specification {

    @Unroll('#input uncapitalized is #output')
    def "uncapitalize the first letter of a CharSequence"(String input, String output) {
        expect:
        output == StringUtils.uncapitalize(input)

        where:
        input           | output
        'H'             | 'h'
        'Hello'         | 'hello'
        'Hello world'   | 'hello world'
        'Hello World'   | 'hello World'
    }

    def "uncapitalize does not thrown exception for null"() {
        when:
        String output = StringUtils.uncapitalize(null)

        then:
        noExceptionThrown()
        output == null

    }
}
