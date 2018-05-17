package com.microsoft.bot.builder.classic.dialogs;

public interface DialogStack
{
/*    /// <summary>
    /// The dialog frames active on the stack.
    /// </summary>
    ReadOnlyList<Delegate> Frames { get; }

    /// <summary>
    /// Suspend the current dialog until an external event has been sent to the bot.
    /// </summary>
    /// <param name="resume">The method to resume when the event has been received.</param>
    void Wait<R>(ResumeAfter<R> resume);

    /// <summary>
    /// Call a child dialog and add it to the top of the stack.
    /// </summary>
    /// <typeparam name="R">The type of result expected from the child dialog.</typeparam>
    /// <param name="child">The child dialog.</param>
    /// <param name="resume">The method to resume when the child dialog has completed.</param>
    void Call<R>(IDialog<R> child, ResumeAfter<R> resume);

    /// <summary>
    /// Post an internal event to the queue.
    /// </summary>
    /// <param name="event">The event to post to the queue.</param>
    /// <param name="resume">The method to resume when the event has been delivered.</param>
    void Post<E>(E @event, ResumeAfter<E> resume);

    /// <summary>
    /// Call a child dialog, add it to the top of the stack and post the item to the child dialog.
    /// </summary>
    /// <typeparam name="R">The type of result expected from the child dialog.</typeparam>
    /// <typeparam name="T">The type of the item posted to child dialog.</typeparam>
    /// <param name="child">The child dialog.</param>
    /// <param name="resume">The method to resume when the child dialog has completed.</param>
    /// <param name="item">The item that will be posted to child dialog.</param>
    /// <param name="token">A cancellation token.</param>
    /// <returns>A task representing the Forward operation.</returns>
    Task Forward<R, T>(IDialog<R> child, ResumeAfter<R> resume, T item, CancellationToken token);

    /// <summary>
    /// Complete the current dialog and return a result to the parent dialog.
    /// </summary>
    /// <typeparam name="R">The type of the result dialog.</typeparam>
    /// <param name="value">The value of the result.</param>
    void Done<R>(R value);

    /// <summary>
    /// Fail the current dialog and return an exception to the parent dialog.
    /// </summary>
    /// <param name="error">The error.</param>
    void Fail(Exception error); */

    /// <summary>
    /// Resets the stack.
    /// </summary>
    void Reset();
}
