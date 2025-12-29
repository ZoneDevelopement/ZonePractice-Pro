package dev.nandi0813.practice.manager.party.matchrequest;

import dev.nandi0813.practice.manager.party.Party;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RequestManager {

    private final Map<Party, Party> pendingRequestTarget = new HashMap<>();
    private final Map<Party, List<PartyRequest>> requests = new HashMap<>();

    public boolean isRequested(Party sender, Party target) {
        if (requests.containsKey(target))
            for (PartyRequest partyRequest : requests.get(target))
                if (partyRequest.getSender().equals(sender))
                    return true;
        return false;
    }

}
