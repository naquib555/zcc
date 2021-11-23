package com.zendesk.zcc.service;

import com.zendesk.zcc.domain.ViewModel;

import java.util.List;

public interface IBaseService {

    ViewModel getAll(Integer page);
    ViewModel getObject(String id);
}
