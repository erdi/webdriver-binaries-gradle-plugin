/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
ruleset {
    ruleset("rulesets/basic.xml")
    ruleset("rulesets/braces.xml")
    ruleset("rulesets/convention.xml") {
        NoDef {
            enabled = false
        }
        TrailingComma {
            enabled = false
        }
    }
    ruleset("rulesets/design.xml")
    ruleset("rulesets/dry.xml") {
        DuplicateStringLiteral {
            doNotApplyToClassNames = '*Spec'
        }
    }
    ruleset("rulesets/exceptions.xml")
    ruleset("rulesets/formatting.xml") {
        ClassJavadoc {
            enabled = false
        }
        SpaceAroundMapEntryColon {
            characterAfterColonRegex = /\s/
        }
        LineLength {
            length = 160
        }
        SpaceAfterOpeningBrace {
            ignoreEmptyBlock = true
        }
        SpaceBeforeClosingBrace {
            ignoreEmptyBlock = true
        }
    }
    ruleset("rulesets/generic.xml")
    ruleset("rulesets/groovyism.xml")
    ruleset("rulesets/imports.xml") {
        MisorderedStaticImports {
            comesBefore = false
        }
        NoWildcardImports {
            enabled = false
        }
    }
    ruleset("rulesets/logging.xml")
    ruleset("rulesets/naming.xml") {
        MethodName {
            regex = /[a-z]\w*( \w+)*/
        }
        FactoryMethodName {
            enabled = false
        }
    }
    ruleset("rulesets/unnecessary.xml")
    ruleset("rulesets/unused.xml")
}