package nl.tudelft.ipv8.android.voting

import android.util.Log
import nl.tudelft.ipv8.Address
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.attestation.trustchain.EMPTY_PK
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class TrustchainVoter() {

    private val trustchain: TrustChainHelper by lazy {
        TrustChainHelper(getTrustChainCommunity())
    }

    private fun getTrustChainCommunity(): TrustChainCommunity {
        return getIpv8().getOverlay()
            ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }

    private fun getIpv8(): IPv8 {
        return IPv8Android.getInstance()
    }

    fun startVote(voteList: List<String>, voteSubject: String) {

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

    }

    fun respondToVote(voteName: String, vote: Boolean, proposalBlock: TrustChainBlock) {
        // Reply to the vote with YES or NO.
        val voteReply = if (vote) "YES" else "NO"

        // Create a JSON object containing the vote subject and the reply.
        val voteJSON = JSONObject()
            .put("VOTE_SUBJECT", voteName)
            .put("VOTE_REPLY", voteReply)

        // Put the JSON string in the transaction's 'message' field.
        val transaction = mapOf("message" to voteJSON.toString())

        trustchain.createAgreementBlock(proposalBlock, transaction)
    }

    /**
     * Return the tally on a vote proposal in a pair(yes, no).
     */
    fun countVotes(voteName: String, proposerKey: ByteArray): Pair<Int, Int> {

        var voters: MutableList<String> = ArrayList()

        var yesCount = 0
        var noCount = 0

        // Crawl the chain of the proposer.
        for (it in trustchain.getChainByUser(proposerKey)) {

            if (voters.contains(it.publicKey.contentToString())){
                continue
            }

            // Skip all blocks which are not voting blocks
            // and don't have a 'message' field in their transaction.
            if (it.type != "voting_block" || !it.transaction.containsKey("message")) {
                continue
            }

            // Parse the 'message' field as JSON.
            val voteJSON = try {
                JSONObject(it.transaction["message"].toString())
            } catch (e: JSONException) {
                // Assume a malicious vote if it claims to be a vote but does not contain
                // proper JSON.
                handleInvalidVote("Block was a voting block but did not contain " +
                    "proper JSON in its message field: ${it.transaction["message"].toString()}."
                )
                continue
            }

            // Assume a malicious vote if it does not have a VOTE_SUBJECT.
            if (!voteJSON.has("VOTE_SUBJECT")) {
                handleInvalidVote("Block type was a voting block but did not have a VOTE_SUBJECT.")
                continue
            }

            // A block with another VOTE_SUBJECT belongs to another vote.
            if (voteJSON.get("VOTE_SUBJECT") != voteName) {
                // Block belongs to another vote.
                continue
            }

            // A block with the same subject but no reply is the original vote proposal.
            if (!voteJSON.has("VOTE_REPLY")) {
                // Block is the initial vote proposal because it does not have a VOTE_REPLY field.
                continue
            }

            // Add the votes, or assume a malicious vote if it is not YES or NO.
            when (voteJSON.get("VOTE_REPLY")) {
                "YES" -> {
                    yesCount++
                    voters.add(it.publicKey.contentToString())
                }
                "NO" -> {
                    noCount++
                    voters.add(it.publicKey.contentToString())
                }
                else -> handleInvalidVote("Vote was not 'YES' or 'NO' but: '${voteJSON.get("VOTE_REPLY")}'.")
            }
        }

        return Pair(yesCount, noCount)
    }

    private fun handleInvalidVote(errorType: String) {
        Log.e("vote_debug", errorType)
    }
}
