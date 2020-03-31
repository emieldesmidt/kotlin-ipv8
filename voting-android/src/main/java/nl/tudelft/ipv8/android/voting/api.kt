package nl.tudelft.ipv8.android.voting

import nl.tudelft.ipv8.attestation.trustchain.EMPTY_PK
import org.json.JSONArray
import org.json.JSONObject

class TrustchainVoting {
    companion object VotingObject {

        protected val trustchain: TrustChainHelper by lazy {
            TrustChainHelper(getTrustChainCommunity())
        }

        fun startVote(voteSubject: String) {
            // TODO: Add vote ID to increase probability of uniqueness.

            // Get all peers in the community and create a JSON array of their public keys.
            val voteList = JSONArray(peers.map { it.publicKey.toString() })

            // Create a JSON object containing the vote subject
            val voteJSON = JSONObject()
                .put("VOTE_SUBJECT", voteSubject)
                .put("VOTE_LIST", voteList)

            // Put the JSON string in the transaction's 'message' field.
            val transaction = mapOf("message" to voteJSON.toString())

            trustchain.createVoteProposalBlock(
                EMPTY_PK,
                transaction,
                "voting_block"
            )

            // Update the JSON to include a VOTE_END message.
            voteJSON.put("VOTE_END", "True")
            val endTransaction = mapOf("message" to voteJSON.toString())

            // Add the VOTE_END transaction to the proposer's chain and self-sign it.
            trustchain.createVoteProposalBlock(
                myPeer.publicKey.keyToBin(),
                endTransaction, "voting_block"
            )
        }
    }
}
