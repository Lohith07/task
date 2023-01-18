package com.template.states;

import com.template.contracts.ManagementContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.CommandAndState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.OwnableState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ManagementContract.class)
public class ManagementState implements LinearState, OwnableState, QueryableState {

    // private variables
    private String title;
    private String description;
    private Party sender;
    private Party receiver;
    private UniqueIdentifier linearId;
    private AbstractParty owner;

    /* Constructor of your Corda state */
    public ManagementState(String title, Party sender, Party receiver,
            UniqueIdentifier linearId) {
        this.title = title;
        this.sender = sender;
        this.receiver = receiver;
        this.linearId = linearId;

    }

    // getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Party getSender() {
        return sender;
    }

    public Party getReceiver() {
        return receiver;
    }

    /*
     * This method will indicate who are the participants and required signers when
     * this state is used in a transaction.
     */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sender, receiver);
    }

    @Override
    public UniqueIdentifier getLinearId() {
        // TODO Auto-generated method stub
        return linearId;
    }

    @Override
    public PersistentState generateMappedObject(MappedSchema arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractParty getOwner() {
        return owner;
    }

    @Override
    public CommandAndState withNewOwner(AbstractParty arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}