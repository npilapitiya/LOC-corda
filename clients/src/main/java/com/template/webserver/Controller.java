package com.template.webserver;

import com.template.flows.CreateAndSendSalesContract;
import com.template.webserver.models.SalesContract;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final CordaX500Name cordaName;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.cordaName = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    @GetMapping(value = "/templateendpoint", produces = "text/plain")
    private String templateendpoint() {
        return "Define an endpoint here.";
    }

    @PostMapping(value = "/SendSalesContract", consumes = "application/json", produces = "application/json", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> GenerateAndSendSalesContract(@RequestBody SalesContract salesContract) throws IllegalArgumentException, IOException {
        try {
            String partyName = salesContract.SellerName;
            String contract = salesContract.ContractName;
            String buyerName = salesContract.BuyerName;

            CordaX500Name partyX500Name = CordaX500Name.parse(buyerName);
            Party buyer = proxy.wellKnownPartyFromX500Name(partyX500Name);
            SignedTransaction result = proxy.startTrackedFlowDynamic(
                    CreateAndSendSalesContract.CreateAndSendSalesContractInitiator.class,
                    contract,
                    buyer
                    ).getReturnValue().get();

           return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction is successful, the trnasaction id is: "+ result.getId().toString());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}