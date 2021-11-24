package com.zendesk.zcc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zendesk.zcc.domain.AppException;
import com.zendesk.zcc.domain.Ticket;
import com.zendesk.zcc.domain.TicketList;
import com.zendesk.zcc.domain.ViewModel;
import com.zendesk.zcc.handler.ZendeskResponseErrorHandler;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class ZendeskApiService {

    private RestTemplate restTemplate;

    @Value("${zendesk.ticket-url}")
    private String zendeskTicketUrl;
    @Value("${zendesk.username}")
    private String zendeskApiUsername;
    @Value("${zendesk.api-token}")
    private String zendeskApiToken;

    @Autowired
    public ZendeskApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .errorHandler(new ZendeskResponseErrorHandler())
                .build();
    }

    /**
     * Returns all the tickets.
     * <p>
     * This method calls the zendesk tickets api
     * and returns list of tickets
     *
     * @return list of the tickets fetched from zendesk api
     */
    public TicketList getAllTickets(Integer page) throws Exception {
        final int perPageTicketCount = 25;

        //building url
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(zendeskTicketUrl)
                .queryParam("per_page", perPageTicketCount);

        if (page != null && page > 0) {
            uriComponentsBuilder.queryParam("page", page);
        }
        UriComponents uri = uriComponentsBuilder.build();

        //get the response body
        JsonNode root = getResponse(uri.toUriString(), zendeskApiUsername, zendeskApiToken);

        ArrayNode ticketsNode = (ArrayNode) root.get("tickets");

        // transforming each json tickets into ticket object and adding into list
        List<Ticket> tickets = new ArrayList<>();
        for (JsonNode element : ticketsNode) {
            tickets.add(transformIntoTicket(element));
        }

        //preparing ticket list
        TicketList ticketList = new TicketList();

        ticketList.setPageTicketCount(tickets.size());
        ticketList.setTotalTickets(root.get("count").asInt());
        ticketList.setCurrentPage(page != null && page > 0 ? page : 1);

        ticketList.setNextPage(getPageNumberFromUrl(root.get("next_page").asText()));
        ticketList.setPreviousPage(getPageNumberFromUrl(root.get("previous_page").asText()));

        //preparing per page ticket count text, which is shown in the UI
        ticketList.setShowingText(String.format("Showing %d to %d of %d",
                (ticketList.getPreviousPage() * perPageTicketCount) + 1,
                ticketList.getCurrentPage() * perPageTicketCount < ticketList.getTotalTickets()
                        ? ticketList.getCurrentPage() * perPageTicketCount : ticketList.getTotalTickets(),
                ticketList.getTotalTickets()
        ));

        ticketList.setTickets(tickets);

        return ticketList;
    }

    /**
     * Returns a specific ticket information.
     * <p>
     * This method calls the zendesk tickets api with the ticket id
     * and returns the information of tickets
     *
     * @param id unique identifier of the ticket
     * @return the detail information of the ticket
     */
    public Ticket getTicket(String id) throws Exception {

        //building url
        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl(zendeskTicketUrl)
                .path(id)
                .build();

        //get the response body
        JsonNode root = getResponse(uri.toUriString(), zendeskApiUsername, zendeskApiToken);

        if (root != null || !root.isNull()) {
            return transformIntoTicket(root.get("ticket"));
        } else return null;
    }

    /**
     * Returns header with basic authentication.
     * <p>
     * This method prepares the basic authentication header
     * in encoded format
     *
     * @param username username of the zendesk api
     * @param token    token of the zendesk api
     * @return http header with basic authentication encoded
     */
    public HttpHeaders createBasicAuthHeader(String username, String token) {
        return new HttpHeaders() {{
            String authentication = username + ":" + token;
            byte[] encodedAuth = Base64.encodeBase64(
                    authentication.getBytes(Charset.forName("US-ASCII")));
            String basicAuthHeader = "Basic " + new String(encodedAuth);
            set("Authorization", basicAuthHeader);
        }};
    }

    /**
     * Returns the response fetched from the api
     * <p>
     * This method prepares and call GET request
     * and fetch the response of the API
     *
     * @param url endpoint
     * @return response of the api in JsonNode
     */
    public JsonNode getResponse(String url, String username, String token) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(createBasicAuthHeader(username,
                        token)),
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    /**
     * Returns ticket information from api response
     * <p>
     * This method parse the ticket information from api response
     * and prepare a ticket object
     *
     * @param element JsonNode object of ticket received in response
     * @return ticket information
     */
    public Ticket transformIntoTicket(JsonNode element) {
        return new Ticket(element.get("id").asText(),
                element.get("subject") != null || !element.get("subject").isNull()
                        ? element.get("subject").asText() : "",
                element.get("description") != null || !element.get("description").isNull()
                        ? element.get("description").asText() : "",
                element.get("status") != null || !element.get("status").isNull()
                        ? element.get("status").asText() : "");
    }

    /**
     * Returns page number from the url
     * <p>
     * This method process the previous/next page url
     * and return the parsed the page number
     *
     * @param url Url from the ticket object when previous/next page is present
     * @return page number
     */
    public Integer getPageNumberFromUrl(String url) {
        MultiValueMap<String, String> pageValueMap =
                UriComponentsBuilder.fromUriString(url).build().getQueryParams();

        return pageValueMap.get("page") != null ? Integer.parseInt(pageValueMap.get("page").get(0)) : 0;
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
