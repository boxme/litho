/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.generator;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.litho.annotations.Event;
import com.facebook.litho.annotations.FromTrigger;
import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.annotations.OnTrigger;
import com.facebook.litho.annotations.Param;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.PropDefault;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.model.SpecModel;
import com.facebook.litho.specmodels.processor.LayoutSpecModelFactory;
import com.google.testing.compile.CompilationRule;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/** Tests {@link TriggerGenerator} */
public class TriggerGeneratorTest {
  @Rule public CompilationRule mCompilationRule = new CompilationRule();

  private final LayoutSpecModelFactory mLayoutSpecModelFactory = new LayoutSpecModelFactory();

  @LayoutSpec
  static class TestSpec<T extends CharSequence> {
    @PropDefault protected static final boolean arg0 = true;

    @OnTrigger(TestEvent.class)
    public Object testTriggerMethod1(
        @Prop boolean arg0,
        @State int arg1,
        @Param Object arg2,
        @Param T arg3,
        @FromTrigger long arg4) {

      return null;
    }

    @OnTrigger(Object.class)
    public void testTriggerMethod2(@Prop boolean arg0, @State int arg1) {}
  }

  @Event(returnType = Object.class)
  static class TestEvent {
    public long arg4;
  }

  private SpecModel mSpecModel;

  @Before
  public void setUp() {
    Elements elements = mCompilationRule.getElements();
    TypeElement typeElement = elements.getTypeElement(TestSpec.class.getCanonicalName());
    mSpecModel = mLayoutSpecModelFactory.create(elements, typeElement, null);
  }

  @Test
  public void testOverrideCanReceiveTriggerMethod() {
    assertThat(TriggerGenerator.overrideCanReceiveTriggerMethod().toString())
        .isEqualTo(
            "@java.lang.Override\n"
                + "protected boolean canAcceptTrigger() {\n"
                + "  return true;\n"
                + "}\n");
  }

  @Test
  public void testGenerateAcceptTriggerEvent() {
    assertThat(TriggerGenerator.generateAcceptTriggerEvent(mSpecModel).toString())
        .isEqualTo(
            "@java.lang.Override\n"
                + "public java.lang.Object acceptTriggerEvent(final com.facebook.litho.EventTrigger eventTrigger,\n"
                + "    final java.lang.Object eventState, final java.lang.Object[] params) {\n"
                + "  int id = eventTrigger.mId;\n"
                + "  switch(id) {\n"
                + "    case -773082596: {\n"
                + "      com.facebook.litho.specmodels.generator.TriggerGeneratorTest.TestEvent _event = (com.facebook.litho.specmodels.generator.TriggerGeneratorTest.TestEvent) eventState;\n"
                + "      return testTriggerMethod1(\n"
                + "            eventTrigger.mTriggerTarget,\n"
                + "            (java.lang.Object) params[0],\n"
                + "            (T) params[1],\n"
                + "            _event.arg4);\n"
                + "    }\n"
                + "    case 969727739: {\n"
                + "      java.lang.Object _event = (java.lang.Object) eventState;\n"
                + "      testTriggerMethod2(\n"
                + "            eventTrigger.mTriggerTarget);\n"
                + "      return null;\n"
                + "    }\n"
                + "    default:\n"
                + "        return null;\n"
                + "  }\n"
                + "}\n");
  }

  @Test
  public void testGenerateOnTriggerMethods() {
    TypeSpecDataHolder dataHolder = TriggerGenerator.generateOnTriggerMethodDelegates(mSpecModel);

    assertThat(dataHolder.getMethodSpecs()).hasSize(2);

    assertThat(dataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "private java.lang.Object testTriggerMethod1(com.facebook.litho.HasEventTrigger _abstractImpl,\n"
                + "    java.lang.Object arg2, T arg3, long arg4) {\n"
                + "  TestImpl _impl = (TestImpl) _abstractImpl;\n"
                + "  java.lang.Object _result = (java.lang.Object) TestSpec.testTriggerMethod1(\n"
                + "    (boolean) _impl.arg0,\n"
                + "    (int) _impl.mStateContainerImpl.arg1,\n"
                + "    arg2,\n"
                + "    arg3,\n"
                + "    arg4);\n"
                + "  return _result;\n"
                + "}\n");

    assertThat(dataHolder.getMethodSpecs().get(1).toString())
        .isEqualTo(
            "private void testTriggerMethod2(com.facebook.litho.HasEventTrigger _abstractImpl) {\n"
                + "  TestImpl _impl = (TestImpl) _abstractImpl;\n"
                + "  TestSpec.testTriggerMethod2(\n"
                + "    (boolean) _impl.arg0,\n"
                + "    (int) _impl.mStateContainerImpl.arg1);\n"
                + "}\n");
  }

  @Test
  public void testGenerateStaticTriggerMethod() {
    TypeSpecDataHolder dataHolder = TriggerGenerator.generateStaticTriggerMethods(mSpecModel);

    assertThat(dataHolder.getMethodSpecs()).hasSize(2);

    assertThat(dataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "public static <T extends java.lang.CharSequence> java.lang.Object testTriggerMethod1(com.facebook.litho.ComponentContext c,\n"
                + "    java.lang.String key, java.lang.Object arg2, T arg3, long arg4) {\n"
                + "  int methodId = -773082596;\n"
                + "  com.facebook.litho.EventTrigger trigger = getEventTrigger(c, methodId, key);\n"
                + "  if (trigger == null) {\n"
                + "    return null;\n"
                + "  }\n"
                + "  com.facebook.litho.specmodels.generator.TriggerGeneratorTest.TestEvent _eventState = new com.facebook.litho.specmodels.generator.TriggerGeneratorTest.TestEvent();\n"
                + "  _eventState.arg4 = arg4;\n"
                + "  return (java.lang.Object) trigger.dispatchOnTrigger(_eventState, new Object[] {\n"
                + "        arg2,\n"
                + "        arg3,\n"
                + "      });\n"
                + "}\n");

    assertThat(dataHolder.getMethodSpecs().get(1).toString())
        .isEqualTo(
            "public static void testTriggerMethod2(com.facebook.litho.ComponentContext c, java.lang.String key) {\n"
                + "  int methodId = 969727739;\n"
                + "  com.facebook.litho.EventTrigger trigger = getEventTrigger(c, methodId, key);\n"
                + "  if (trigger == null) {\n"
                + "    return;\n"
                + "  }\n"
                + "  java.lang.Object _eventState = new java.lang.Object();\n"
                + "  trigger.dispatchOnTrigger(_eventState, new Object[] {\n"
                + "      });\n"
                + "}\n");
  }
}
