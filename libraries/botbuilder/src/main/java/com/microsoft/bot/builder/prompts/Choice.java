import com.microsoft.bot.schema.models.CardAction;

import java.util.ArrayList;

public class Choice
{
    ///<summary>
    /// Value to return when selected.
    ///</summary>
    String _value;
    public void setValue(String value) {
        this._value = value;
    }
    public String getValue() {
        return this._value;
    }

    ///<summary>
    /// (Optional) action to use when rendering the choice as a suggested action.
    ///</summary>
    CardAction _action;
    public CardAction getAction() {
        return this._action;
    }
    public void setAction(CardAction action) {
        this._action = action;
    }

    ///<summary>
    /// (Optional) list of synonyms to recognize in addition to the value.
    ///</summary>
    ArrayList<String> _synonyms;
    public ArrayList<String> getSynonyms() {
        return _synonyms;
    }
    public void setSynonyms(ArrayList<String> synonyms) {
        this._synonyms = synonyms;
    }
}
