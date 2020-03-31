package nl.tudelft.ipv8.android.voting

import nl.tudelft.ipv8.Address
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import java.util.*

class VotingCommunity : Community() {
    override val serviceId = "02313685c1912a141279f8248fc8db5899c5d008"

    val discoveredAddressesContacted: MutableMap<Address, Date> = mutableMapOf()

//    protected val trustchain: TrustChainHelper by lazy {
//        TrustChainHelper(getTrustChainCommunity())
//    }

    override fun walkTo(address: Address) {
        super.walkTo(address)
        discoveredAddressesContacted[address] = Date()
    }

//    protected fun getTrustChainCommunity(): TrustChainCommunity {
//        return getIpv8().getOverlay()
//            ?: throw IllegalStateException("TrustChainCommunity is not configured")
//    }

//    protected fun getVotingCommunity(): VotingCommunity {
//        return getIpv8().getOverlay()
//            ?: throw IllegalStateException("VotingCommunity is not configured")
//    }

//    protected fun getIpv8(): IPv8 {
//        return IPv8Android.getInstance()
//    }

    val tvoter: TrustChainVoter = TrustChainVoter()

    fun startVote(voters : List<String>, voteSubject: String) = tvoter.startVote(voters, voteSubject)

    fun respondToVote(voteName: String, vote: Boolean, proposalBlock: TrustChainBlock) =  tvoter.respondToVote(voteName, vote, proposalBlock)

    fun countVotes(voters: List<String>, voteName: String, proposerKey: ByteArray): Pair<Int, Int> = countVotes(voters, voteName, proposerKey)

    class Factory : Overlay.Factory<VotingCommunity>(VotingCommunity::class.java)

}
