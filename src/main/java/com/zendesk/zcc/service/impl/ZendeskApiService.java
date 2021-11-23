package com.zendesk.zcc.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zendesk.zcc.config.ZendeskPropertiesReader;
import com.zendesk.zcc.domain.AppException;
import com.zendesk.zcc.domain.Ticket;
import com.zendesk.zcc.domain.TicketList;
import com.zendesk.zcc.handler.ZendeskResponseErrorHandler;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger logger = LoggerFactory.getLogger(ZendeskApiService.class);

    private RestTemplate restTemplate;
    private ZendeskPropertiesReader zendeskPropertiesReader;

    public ZendeskApiService(RestTemplateBuilder restTemplateBuilder, ZendeskPropertiesReader zendeskPropertiesReader) {
        this.restTemplate = restTemplateBuilder
                .errorHandler(new ZendeskResponseErrorHandler())
                .build();
        this.zendeskPropertiesReader = zendeskPropertiesReader;
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

        String ticketUrl = zendeskPropertiesReader.getBaseUrl() + zendeskPropertiesReader.getTicket();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(ticketUrl)
                .queryParam("per_page", perPageTicketCount);

        if (page != null && page > 0) {
            uriComponentsBuilder.queryParam("page", page);
        }
        UriComponents uri = uriComponentsBuilder.build();
        JsonNode root = getResponse(uri.toUriString());

        ArrayNode ticketsNode = (ArrayNode) root.get("tickets");

        List<Ticket> tickets = new ArrayList<>();
        for (JsonNode element : ticketsNode) {
            tickets.add(transformIntoTicket(element));
        }

        MultiValueMap<String, String> nextPageValueMap =
                UriComponentsBuilder.fromUriString(root.get("next_page").asText()).build().getQueryParams();
        MultiValueMap<String, String> previousPageValueMap =
                UriComponentsBuilder.fromUriString(root.get("previous_page").asText()).build().getQueryParams();

        TicketList ticketList = new TicketList();
        ticketList.setPageTicketCount(tickets.size());
        ticketList.setTotalTickets(root.get("count").asInt());
        ticketList.setPreviousPage(previousPageValueMap.get("page") != null ? Integer.parseInt(previousPageValueMap.get("page").get(0)) : 0);
        ticketList.setNextPage(nextPageValueMap.get("page") != null ? Integer.parseInt(nextPageValueMap.get("page").get(0)) : 0);
        ticketList.setCurrentPage(page != null && page > 0 ? page : 1);
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
        String url = zendeskPropertiesReader.getBaseUrl() + zendeskPropertiesReader.getTicket() + "/" + id;
        JsonNode root = getResponse(url);

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
    public JsonNode getResponse(String url) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(createBasicAuthHeader(zendeskPropertiesReader.getUsername(),
                        zendeskPropertiesReader.getToken())),
                String.class);

        logger.info("Status: " + response.getStatusCode());

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    /**
     * Returns true if status found OK
     * <p>
     * This method checks the http status of the api
     * and throws exception if invalid response found
     *
     * @param status http status code of the api response
     * @return boolean value based on the status code
     */
    public boolean checkResponseStatus(HttpStatus status) {
        if (status.equals(HttpStatus.OK)) return true;
        else if (status.equals(HttpStatus.UNAUTHORIZED))
            throw new AppException("Application facing authentication error with service provider");
        else return false;
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
                        ? element.get("subject").asText() : "",
                element.get("status") != null || !element.get("status").isNull()
                        ? element.get("status").asText() : "");
    }
}
