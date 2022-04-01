/*
 * Copyright 2021 rma.
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
package ch.uprisesoft.yali.scope;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.Call;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rma
 */
public class Scope {

    private String scopeName = "";
    private Map<String, Node> members = new HashMap<>();
    
    private Node code = Node.nil();

    public Scope(String scopeName) {
        this.scopeName = scopeName;
    }
    
    public Scope(Call code, String scopeName) {
        this.code = code;
        this.scopeName = scopeName;
    }

    public String getScopeName() {
        return scopeName;
    }

    public Node getCode() {
        return code;
    }
    
    protected Map<String, Node> members() {
        return members;
    }

    public Node resolve(String name) {
        if (members.containsKey(name.toLowerCase())) {
            return members.get(name.toLowerCase());
        } 

        return Node.none();
    }

    public void define(String name, Node value) {
        members.put(name.toLowerCase(), value);
    }

    public void local(String name) {
        members.put(name.toLowerCase(), Node.none());
    }

    public boolean defined(String name) {
        return members.containsKey(name);
    }

}
