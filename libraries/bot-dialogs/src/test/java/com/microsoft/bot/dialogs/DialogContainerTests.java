// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;

public class DialogContainerTests {

    @Test
    public void DialogContainer_GetVersion() {
        TestContainer ds = new TestContainer();
        String version1 = ds.getInternalVersion_Test();
        Assert.assertNotNull(version1);

        TestContainer ds2 = new TestContainer();
        String version2 = ds.getInternalVersion_Test();
        Assert.assertNotNull(version2);
        Assert.assertEquals(version1, version2);

        DialogTestFunction testFunction = testFunction1 -> {
                                    return CompletableFuture.completedFuture(null);
                                };
        LamdbaDialog ld = new LamdbaDialog("Lamdba1", testFunction);
        ld.setId("A");

        ds2.getDialogs().add(ld);
        String version3 = ds2.getInternalVersion_Test();
        Assert.assertNotNull(version3);
        Assert.assertNotEquals(version2, version3);

        String version4 = ds2.getInternalVersion_Test();
        Assert.assertNotNull(version3);
        Assert.assertEquals(version3, version4);

        TestContainer ds3 = new TestContainer();
        DialogTestFunction testFunction2 = testFunction1 -> {
            return CompletableFuture.completedFuture(null);
        };
        LamdbaDialog ld2 = new LamdbaDialog("Lamdba1", testFunction2);
        ld2.setId("A");
        ds3.getDialogs().add(ld2);

        String version5 = ds3.getInternalVersion_Test();
        Assert.assertNotNull(version5);
        Assert.assertEquals(version5, version4);

        ds3.setProperty("foobar");
        String version6 = ds3.getInternalVersion_Test();
        Assert.assertNotNull(version6);
        Assert.assertNotEquals(version6, version5);

        TestContainer ds4 = new TestContainer();
        ds4.setProperty("foobar");

        DialogTestFunction testFunction3 = testFunction1 -> {
            return CompletableFuture.completedFuture(null);
        };
        LamdbaDialog ld3 = new LamdbaDialog("Lamdba1", testFunction3);
        ld3.setId("A");

        ds4.getDialogs().add(ld3);
        String version7 = ds4.getInternalVersion_Test();
        Assert.assertNotNull(version7);
        Assert.assertEquals(version7, version6);
    }

    public class TestContainer extends DialogContainer {

        private String property;

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            return dc.endDialog();
        }

        @Override
        public DialogContext createChildContext(DialogContext dc) {
            return dc;
        }

        public String getInternalVersion_Test() {
            return getInternalVersion();
        }

        @Override
        protected String getInternalVersion() {
            StringBuilder result =  new StringBuilder();
            result.append(super.getInternalVersion());
            if (getProperty() != null) {
                result.append(getProperty());
            }
            return result.toString();
        }

        /**
         * @return the Property value as a String.
         */
        public String getProperty() {
            return this.property;
        }

        /**
         * @param withProperty The Property value.
         */
        public void setProperty(String withProperty) {
            this.property = withProperty;
        }
    }
}
