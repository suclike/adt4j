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

package com.github.sviperll.codemold;

import com.github.sviperll.codemold.render.Renderable;
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.Strings;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class AnyAnnotationValue
        implements AnnotationValue, Renderable {

    static AnyAnnotationValue wrapPrimitive(PrimitiveAnnotationValue value) {
        return new PrimitiveAnnotationValueWrapper(value);
    }

    static AnyAnnotationValue of(String value) {
        return new StringAnnotationValueWrapper(value);
    }

    static AnyAnnotationValue of(EnumConstant value) {
        return new EnumConstantAnnotationValueWrapper(value);
    }

    static AnyAnnotationValue of(ObjectDefinition value) {
        return new ObjectDefinitionAnnotationValueWrapper(value);
    }

    static AnyAnnotationValue of(Annotation value) {
        return new AnnotationAnnotationValueWrapper(value);
    }

    static AnyAnnotationValue wrapArray(ArrayAnnotationValue value) {
        return new AnyAnnotationValueWrapper(value);
    }

    private final Kind kind;

    private AnyAnnotationValue(Kind kind) {
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public PrimitiveAnnotationValue getPrimitive() {
        throw new UnsupportedOperationException("Is not primitive");
    }

    public String getString() {
        throw new UnsupportedOperationException("Is not string");
    }

    public EnumConstant getEnumConstant() {
        throw new UnsupportedOperationException("Is not EnumConstant");
    }

    public ObjectDefinition getObjectDefinition() {
        throw new UnsupportedOperationException("Is not ObjectDefinition");
    }

    public Annotation getAnnotation() {
        throw new UnsupportedOperationException("Is not Annotation");
    }

    public ArrayAnnotationValue getArray() {
        throw new UnsupportedOperationException("Is not array");
    }

    @Override
    public AnyAnnotationValue asAny() {
        return this;
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (kind.isAnnotation())
                    context.appendRenderable(getAnnotation());
                else if (kind.isArray())
                    context.appendRenderable(getArray());
                else if (kind.isEnumConstant()) {
                    context.appendRenderable(getEnumConstant().enumDefinition().rawType());
                    context.appendText(".");
                    context.appendText(getEnumConstant().name());
                } else if (kind.isObjectDefinition())
                    context.appendRenderable(getObjectDefinition().classLiteral());
                else if (kind.isPrimitive())
                    context.appendRenderable(getPrimitive());
                else if (kind.isString())
                    context.appendText(Strings.quote(getString()));
                else
                    throw new UnsupportedOperationException("Unsupported kind " + kind);
            }
        };
    }

    public enum Kind {
        PRIMITIVE,
        STRING,
        ENUM_CONSTANT,
        OBJECT_DEFINITION,
        ANNOTATION,
        ARRAY;

        public boolean isPrimitive() {
            return this == PRIMITIVE;
        }

        public boolean isString() {
            return this == STRING;
        }

        public boolean isEnumConstant() {
            return this == ENUM_CONSTANT;
        }

        public boolean isObjectDefinition() {
            return this == OBJECT_DEFINITION;
        }

        public boolean isAnnotation() {
            return this == ANNOTATION;
        }

        public boolean isArray() {
            return this == ARRAY;
        }
    }

    private static class PrimitiveAnnotationValueWrapper extends AnyAnnotationValue {

        private final PrimitiveAnnotationValue value;

        PrimitiveAnnotationValueWrapper(PrimitiveAnnotationValue value) {
            super(Kind.PRIMITIVE);
            this.value = value;
        }

        @Override
        public PrimitiveAnnotationValue getPrimitive() {
            return value;
        }
    }

    private static class StringAnnotationValueWrapper extends AnyAnnotationValue {
        private final String value;

        public StringAnnotationValueWrapper(String value) {
            super(Kind.STRING);
            this.value = value;
        }

        @Override
        public String getString() {
            return value;
        }
    }

    private static class EnumConstantAnnotationValueWrapper extends AnyAnnotationValue {

        private final EnumConstant value;

        public EnumConstantAnnotationValueWrapper(EnumConstant value) {
            super(Kind.ENUM_CONSTANT);
            this.value = value;
        }
        @Override
        public EnumConstant getEnumConstant() {
            return value;
        }
    }

    private static class ObjectDefinitionAnnotationValueWrapper extends AnyAnnotationValue {

        private final ObjectDefinition value;

        public ObjectDefinitionAnnotationValueWrapper(ObjectDefinition value) {
            super(Kind.OBJECT_DEFINITION);
            this.value = value;
        }
        @Override
        public ObjectDefinition getObjectDefinition() {
            return value;
        }
    }

    private static class AnnotationAnnotationValueWrapper extends AnyAnnotationValue {

        private final Annotation value;

        public AnnotationAnnotationValueWrapper(Annotation value) {
            super(Kind.ANNOTATION);
            this.value = value;
        }

        @Override
        public Annotation getAnnotation() {
            return value;
        }
    }

    private static class AnyAnnotationValueWrapper extends AnyAnnotationValue {

        private final ArrayAnnotationValue value;

        public AnyAnnotationValueWrapper(ArrayAnnotationValue value) {
            super(Kind.ARRAY);
            this.value = value;
        }

        @Override
        public ArrayAnnotationValue getArray() {
            return value;
        }
    }
}