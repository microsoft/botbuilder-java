/*
public class DialogContainer implements IDialogContinue


{
    protected DialogSet Dialogs { get; set; }
    protected string DialogId { get; set; }

    public DialogContainer(string dialogId, DialogSet dialogs = null)
    {
        if (string.IsNullOrEmpty(dialogId))
            throw new ArgumentNullException(nameof(dialogId));

        Dialogs dialogs = (dialogs != null) ? dialogs : new DialogSet();
        DialogId = dialogId;
    }

    public async Task DialogBegin(DialogContext dc, IDictionary<string, object> dialogArgs = null)
    {
        if (dc == null)
            throw new ArgumentNullException(nameof(dc));

        // Start the controls entry point dialog. 
        IDictionary<string, object> result = null;
        var cdc = new DialogContext(this.Dialogs, dc.Context, dc.ActiveDialog.State, (r) => { result = r; });
        await cdc.Begin(DialogId, dialogArgs);
        // End if the controls dialog ends.
        if (cdc.ActiveDialog == null)
        {
            await dc.End(result);
        }
    }

    public async Task DialogContinue(DialogContext dc)
    {
        if (dc == null)
            throw new ArgumentNullException(nameof(dc));

        // Continue controls dialog stack.
        IDictionary<string, object> result = null;
        var cdc = new DialogContext(this.Dialogs, dc.Context, dc.ActiveDialog.State, (r) => { result = r; });
        await cdc.Continue();
        // End if the controls dialog ends.
        if (cdc.ActiveDialog == null)
        {
            await dc.End(result);
        }
    }
}
*/
