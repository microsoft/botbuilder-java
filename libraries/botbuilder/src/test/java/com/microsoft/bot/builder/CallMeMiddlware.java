package com.microsoft.bot.builder;

public class CallMeMiddlware implements Middleware {
    private ActionDel callMe;

    public CallMeMiddlware(ActionDel callme) {
        this.callMe = callme;
    }

    @Override
    public void OnTurn(TurnContext context, NextDelegate next) throws Exception {

        this.callMe.CallMe();
        try {
            next.next();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
