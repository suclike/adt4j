/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.codemodel;

import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class GenericsConfig {
    GenericsConfig() {
    }

    public abstract GenericsConfig parent();

    public abstract List<TypeParameter> typeParameters();

    public abstract TypeParameter get(String name);

    final GenericsConfig preventCycle(String name) {
        return new PreventCycleEnvironment(this, name);
    }

    final Type lowerRawBound(String name) throws CodeModelException {
        TypeParameter parameter;
        try {
            parameter = get(name);
        } catch (RuntimeCodeModelException ex) {
            throw ex.getCause();
        }
        if (parameter == null)
            throw new CodeModelException(name + " name is not found in environment");
        GenericsConfig environment = parameter.declaredIn().generics().preventCycle(name);
        Type bound = parameter.bound();
        if (bound.isTypeVariable()) {
            return environment.lowerRawBound(bound.getVariableDetails().name());
        } else {
            ObjectTypeDetails lower = null;
            for (Type type: bound.asListOfIntersectedTypes()) {
                ObjectTypeDetails object = type.getObjectDetails();
                if (lower == null || lower.definition().extendsOrImplements(object.definition()))
                    lower = object;
            }
            if (lower == null)
                throw new CodeModelException("Empty bounds found for variable");
            return lower.asType();
        }
    }

    public boolean isGeneric() {
        if (!typeParameters().isEmpty())
            return true;
        else {
            GenericsConfig parent = parent();
            return parent != null && parent.isGeneric();
        }
    }

    private static class PreventCycleEnvironment extends GenericsConfig {
        private final GenericsConfig parent;
        private final String name;

        public PreventCycleEnvironment(GenericsConfig parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        @Override
        public GenericsConfig parent() {
            return parent;
        }

        @Override
        public List<TypeParameter> typeParameters() {
            return Collections.emptyList();
        }

        @Override
        public TypeParameter get(String name) {
            try {
                if (!name.equals(this.name))
                    return parent.get(name);
                else {
                    throw new CodeModelException("Cyclic definition: " + name);
                }
            } catch (CodeModelException ex) {
                throw new RuntimeCodeModelException(ex);
            }
        }
    }
}