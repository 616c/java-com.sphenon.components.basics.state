package com.sphenon.basics.state.classes;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.expression.*;
import com.sphenon.basics.expression.classes.*;
import com.sphenon.basics.expression.returncodes.*;
import com.sphenon.basics.data.*;

import com.sphenon.basics.state.*;

import java.util.Vector;

public class StateComplexCondition implements StateCondition {

    static public boolean matches(CallContext context, String condition, State state) {
        return (new StateComplexCondition(context, condition)).matches(context, state);
    }

    protected com.sphenon.basics.expression.parsed.Expression expression;

    public StateComplexCondition (CallContext context, String condition) {
        this.condition = condition;
        this.condition_source = null;
        this.is_volatile = false;
        this.parse(context);
    }

    @com.sphenon.engines.aggregator.annotations.OCPIgnore
    public StateComplexCondition (CallContext context, DataSource<String> condition_source) {
        this.condition = null;
        this.condition_source = condition_source;
        this.is_volatile = false;
    }

    @com.sphenon.engines.aggregator.annotations.OCPIgnore
    public StateComplexCondition (CallContext context, DataSource<String> condition_source, boolean is_volatile) {
        this.condition = null;
        this.condition_source = condition_source;
        this.is_volatile = is_volatile;
    }

    protected void parse(CallContext context) {
        String condition = this.getCondition(context);
        try {
            this.expression = com.sphenon.basics.expression.parsed.ExpressionParser.parse(context, condition);
        } catch (com.sphenon.basics.expression.parsed.ParseException pe) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, pe, "Syntax error in state condition '%(condition)'", "condition", condition);
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
    }

    protected String condition;
    protected DataSource<String> condition_source;
    protected boolean is_volatile;

    public String getCondition (CallContext context) {
        if (    (this.condition == null || this.is_volatile)
             && this.condition_source != null) {
            this.condition = this.condition_source.get(context);
        }
        return this.condition;
    }

    public String toString() {
        return "[StateCondition: '" + this.condition + "']";
    }

    public boolean matches(CallContext context, State state) {
        if ((state instanceof StateComplex) == false) { return false; }
        final StateComplex sc = (StateComplex) state;

        try {
            if (this.expression == null || this.is_volatile) {
                this.parse(context);
            }
            boolean result =
                this.expression.isTrue(context, new Class_Scope(context) {
                        protected Result doGetVariable (CallContext context, String name, String search_name_space) {
                            if (search_name_space == null || search_name_space.isEmpty()) {
                                String value = sc.tryGetValue(context, "main");
                                return new Result(new Boolean(name.equals(value)));
                            } else if (search_name_space.equals("LHS")) {
                                return new Result(sc.tryGetValue(context, name));
                            } else if (search_name_space.equals("RHS")) {
                                return new Result(name);
                            } else {
                                return null;
                            }
                        }
                        public Vector<Variable> getAllVariables(CallContext context, String pattern) {
                            Vector<Variable> result = new Vector<Variable>();
                            for (String[] matching : sc.getValues(context, pattern)) {
                                result.add(new Class_Variable(context, matching[0], null, matching[1]));
                            }
                            return result;
                        }
                    }
                );
            return result;
        } catch (EvaluationFailure ef) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ef, "Evaluation of state condition '%(condition)' failed", "condition", this.condition);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }
}
