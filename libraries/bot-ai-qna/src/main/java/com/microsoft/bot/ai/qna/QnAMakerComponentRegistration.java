// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.microsoft.bot.ai.qna.dialogs.QnAMakerDialog;

/**
 * Class which contains registration of components for QnAMaker.
 */
// TODO: missing ComponentRegistration, DeclarativeType, ResourceExplorer
// classes and
// IComponentDeclarativeTypes interface
public class QnAMakerComponentRegistration extends ComponentRegistration implements IComponentDeclarativeTypes {
    /**
     * Gets declarative type registrations for QnAMAker.
     * @param resourceExplorer resourceExplorer to use for resolving references.
     * @return enumeration of DeclarativeTypes.
     */
    public DeclarativeType[] getDeclarativeTypes(ResourceExplorer resourceExplorer) {
        String qnaMakerDialogKind = QnAMakerDialog.getKind();
        String qnaMakerDialogClassname = QnAMakerDialog.class.getName();
        String qnaMakerRecognizerKind = QnAMakerRecognizer.getKind();
        String qnaMakerRecognizerClassname = QnAMakerRecognizer.class.getName();
        DeclarativeType[] declarativeTypes = {
            // Dialogs
            new DeclarativeType(){
                setKind(qnaMakerDialogKind);
                setType(qnaMakerDialogClassname);
            },
            // Recognizers
            new DeclarativeType(){
                setKind(qnaMakerRecognizerKind);
                setType(qnaMakerRecognizerClassname);
            }
        };

        return declarativeTypes;
    }
}
