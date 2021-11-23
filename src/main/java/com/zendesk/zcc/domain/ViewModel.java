package com.zendesk.zcc.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ViewModel<T> {

    private boolean status;
    private String message;
    private T data;
}
