package com.microsoft.bot.dialogs;

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.skills.SkillHandler;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.SkillValidation;

/**
 * A class to contain code that is duplicated across multiple Dialog related
 * classes and can be shared through this common class.
 */
final class DialogCommon {

    private DialogCommon() {

    }

    /**
     * Determine if a turnContext is from a Parent to a Skill.
     * @param turnContext the turnContext.
     * @return true if the turnContext is from a Parent to a Skill, false otherwise.
     */
    static boolean isFromParentToSkill(TurnContext turnContext) {
        if (turnContext.getTurnState().get(SkillHandler.SKILL_CONVERSATION_REFERENCE_KEY) != null) {
            return false;
        }

        Object identity = turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY);
        if (identity instanceof ClaimsIdentity) {
            ClaimsIdentity claimsIdentity = (ClaimsIdentity) identity;
            return SkillValidation.isSkillClaim(claimsIdentity.claims());
        } else {
            return false;
        }
    }
}
