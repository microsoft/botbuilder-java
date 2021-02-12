package com.microsoft.bot.ai.luis.testdata;

import com.microsoft.bot.builder.RecognizerConvert;
import com.microsoft.bot.builder.RecognizerResult;

public class TestRecognizerResultConvert implements RecognizerConvert {

    public String recognizerResultText;

    @Override
    public void convert(Object result) {
        RecognizerResult castedObject  = ((RecognizerResult) result);
        recognizerResultText = castedObject.getText();
    }
}
