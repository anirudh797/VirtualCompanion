
package com.example.virtualcompanion.view

/**
 * Interface that defines how to handle interaction with a RecyclerView list or one of its elements.
 * This class has a generic argument which should evaluate to the list's elements class.
 *
 */
interface ListInteractionListener<T> {
    /**
     * Called when a list element is clicked.
     *
     * @param item the clicked item.
     */
    fun onItemClick(item: T)

    /**
     * Called when the list elements are being fetched.
     */
    fun startLoading()

    /**
     * Called when one or all the list elements have been fetched.
     *
     * @param partialResults true if the results are partial and
     * the fetching is still going, false otherwise.
     */
    fun endLoading(partialResults: Boolean)

    /**
     * Called to dismiss a loading dialog.
     *
     * @param error   true if an error has occurred, false otherwise.
     * @param element the list element processed.
     */
    fun endLoadingWithDialog(error: Boolean, element: T)
}