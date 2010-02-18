package com.todoroo.androidcommons.service;

import java.io.IOException;

public class HttpUnavailableException extends IOException {

    private static final long serialVersionUID = 5373340422464657279L;

    public HttpUnavailableException() {
        super();
        DependencyInjectionService.getInstance().inject(this);
    }

    @Override
    public String getMessage() {
        return "Sorry, our servers are experiencing some issues. Please try again later!"; //$NON-NLS-1$ // FIXME
    }

}
