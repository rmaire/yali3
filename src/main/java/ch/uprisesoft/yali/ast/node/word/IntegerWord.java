/* 
 * Copyright 2020 Uprise Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.uprisesoft.yali.ast.node.word;

import ch.uprisesoft.yali.ast.node.NodeType;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class IntegerWord extends Word {

    public IntegerWord(Integer intVal) {
        super(NodeType.INTEGER);
        this.integerWord = intVal;
        this.stringWord = intVal.toString();
        this.floatWord = Double.valueOf(intVal.floatValue());
    }

    @Override
    public String toString() {
        return integerWord.toString();
    }
}
