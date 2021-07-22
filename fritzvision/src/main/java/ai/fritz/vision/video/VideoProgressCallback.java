package ai.fritz.vision.video;

interface VideoProgressCallback<T> {

    /**
     * Notifies the user on processing progress.
     *
     * @param response Data to indicate progress.
     */
    void onProgress(T response);

    /**
     * Notifies the user when the process is completed.
     */
    void onComplete();
}
