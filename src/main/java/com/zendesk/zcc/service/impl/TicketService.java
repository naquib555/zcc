package com.zendesk.zcc.service.impl;

import com.zendesk.zcc.domain.AppException;
import com.zendesk.zcc.domain.Ticket;
import com.zendesk.zcc.domain.TicketList;
import com.zendesk.zcc.domain.ViewModel;
import com.zendesk.zcc.service.IBaseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService implements IBaseService {

    private ZendeskApiService zendeskApiService;

    public TicketService(ZendeskApiService zendeskApiService) {
        this.zendeskApiService = zendeskApiService;
    }

    /**
     * Returns all the tickets and message if any error occurs.
     * <p>
     * This method fetch information of all the tickets
     *
     * @return ViewModel object for all the tickets
     */
    @Override
    public ViewModel<TicketList> getAll(Integer page) {
        ViewModel<TicketList> viewModel = new ViewModel<>();
        TicketList ticketList = new TicketList();
        try {
            ticketList = zendeskApiService.getAllTickets(page);
            if(ticketList.getTickets().size() > 0) {
                viewModel.setStatus(true);
            } else {
                viewModel.setStatus(false);
                viewModel.setMessage("No tickets found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleException(viewModel, e);
        }

        viewModel.setData(ticketList);

        return viewModel;
    }

    /**
     * Returns viewModel of ticket information and message if any error occurs.
     * <p>
     * This method returns ticket information if exists
     *
     * @param id unique identifier of the ticket
     * @return ViewModel object of ticket
     */
    @Override
    public ViewModel getObject(String id) {
        ViewModel<Ticket> viewModel = new ViewModel<>();

        try {
            Ticket ticket = zendeskApiService.getTicket(id);
            viewModel.setStatus(true);
            viewModel.setData(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            handleException(viewModel, e);
        }

        return viewModel;
    }

    /**
     * Returns viewModel with error message.
     * <p>
     * This method prepares the message of the exception and set in viewModel
     *
     * @param viewModel ViewModel object
     * @param e The exception that occurred
     * @return ViewModel object with exception message and status
     */
    public ViewModel handleException(ViewModel viewModel, Exception e) {
        viewModel.setStatus(false);

        if(e instanceof AppException)
            viewModel.setMessage(e.getMessage());
        else
            viewModel.setMessage("Application is facing internal issues. Please try later");

        return viewModel;
    }
}
