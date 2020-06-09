import java.util.*;


/**
 OLD AGENT
 
 NO HELPER METHODS added to this version
 */
 
 
 
public class Old_PSU_id_963827403_GinRummyPlayerV1 implements GinRummyPlayer {
    private int playerNum;
    @SuppressWarnings("unused")
    private int startingPlayerNum;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private Random random = new Random();
    private boolean opponentKnocked = false;
    Card faceUpCard, drawnCard;
    ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();

    //Create additional data structures to help with new functionalities

    //lists to reference edges and middle pieces by rank
    List<String> edges = Arrays.asList("A", "2", "3", "J", "Q", "K");
    List<String> middles = Arrays.asList("4", "5", "6", "7", "8", "9");

    int k = 1;


    //------------This did NOT work. Please ignore.--------
    //lists to reference cards with high deadwood points and low points
    // List<String> low = Arrays.asList("A", "1", "2", "3");
    // List<String> high = Arrays.asList("10", "J", "Q", "K");
    //  ------------This did NOT work. Please ignore.--------


    //map to store possible candidate cards to discard (as keys) and an integer
    // which tells us how likely the card is to form a future meld (as values)
    Map<Card, Float> CandidateCardsWithMatches = new HashMap<Card, Float>();

    //boolean which will tell us if the current card being drawn is a middle card
    private boolean middle = false;

    private boolean higher = false;
    private boolean inBest = false;

    ArrayList<ArrayList<ArrayList<Card>>> bestify = new ArrayList<ArrayList<ArrayList<Card>>>();
    ArrayList<Card> onlyDeadwood = (ArrayList<Card>) cards.clone();


    //    ------------This did NOT work. Please ignore.--------
//boolean which will tell us if the current card being drawn is a low rank card
    // private boolean lowdraw = false;
    //      ------------This did NOT work. Please ignore.--------


    ArrayList<State> gameLog = new ArrayList<State>();

    private ArrayList<Card> seen = new ArrayList<Card>();

    private ArrayList<Card> otherCards = new ArrayList<Card>();

    int count = 51;

    int round = 0;


    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        this.playerNum = playerNum;
        this.startingPlayerNum = startingPlayerNum;
        this.cards.clear();
        for (Card card : cards)
            this.cards.add(card);
        opponentKnocked = false;
        drawDiscardBitstrings.clear();

        this.count = 51;
        this.round = 0;
        this.gameLog.clear();
        this.otherCards.clear();
        this.seen.clear();


    }

    @Override
    public boolean willDrawFaceUpCard(Card card) {
        // Return true if card would be a part of a meld, false otherwise.
        this.faceUpCard = card;
        @SuppressWarnings("unchecked")
        ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
        newCards.add(card);
        bestify = GinRummyUtil.cardsToBestMeldSets(newCards);

/*
        for (Card s : cards) {  //eliminate melded cards
            for (int i = 0; i < bestify.size(); i++) {
                for (int x = 0; x < bestify.get(i).size(); x++) {
                    // for (int z = 0; z < bestify.get(i).size(); z++) {
                    if ((bestify.get(i).get(x).contains(s)))
                        onlyDeadwood.remove(s);
                }
            }
        }
int max_rank = -1;
        Card highest = card;
        for(Card x: onlyDeadwood)
            if(x.rank>0) {
                max_rank = x.rank;
                highest = x;
            }
        if(highest.rank > card.rank)
            true;
        else
            return false;
*/


        for (int i = 0; i < bestify.size(); i++) {
            for (int x = 0; x < bestify.get(i).size(); x++) {
                // for (int z = 0; z < bestify.get(i).size(); z++) {
                if ((bestify.get(i).get(x).contains(card)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void reportDraw(int playerNum, Card drawnCard) {
        // Ignore other player draws.  Add to cards if playerNum is this player.
        if (playerNum == this.playerNum) {
            cards.add(drawnCard);
            this.drawnCard = drawnCard;
            ///classify the drawn card as middle or not
            if (edges.contains(drawnCard.rank))
                middle = true;

            if (!edges.contains(drawnCard.rank))
                middle = false;


            //      ------------This did NOT work. Please ignore.--------
            //classify the drawn card as low or not
            //if(low.contains(drawnCard.rank))
            //    lowdraw = true;

            // if(!low.contains(drawnCard.rank))
            //     lowdraw = false;
            //        ------------This did NOT work. Please ignore.--------


        }


        if (playerNum != this.playerNum)
            otherCards.add(drawnCard);


        //decrease face down card count by 1
        if (count > 2)
            count--;

        if (!(seen.contains(drawnCard)))
            seen.add(drawnCard);


    }

    @SuppressWarnings("unchecked")
    @Override
    public Card getDiscard() {
        int turns = 0;
        // Discard a random card (not just drawn face up) leaving minimal deadwood points.
        int minDeadwood = Integer.MAX_VALUE;
        ArrayList<Card> candidateCards = new ArrayList<Card>();
        for (Card card : cards) {
            // Cannot draw and discard face up card.
            if (card == drawnCard && drawnCard == faceUpCard)
                continue;
            // Disallow repeat of draw and discard.
            ArrayList<Card> drawDiscard = new ArrayList<Card>();
            drawDiscard.add(drawnCard);
            drawDiscard.add(card);
            if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
                continue;

            ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
            remainingCards.remove(card);
            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
            int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
            if (deadwood <= minDeadwood) {
                if (deadwood < minDeadwood) {
                    minDeadwood = deadwood;
                    candidateCards.clear();
                }
                candidateCards.add(card);
            }
        }
        Card discard = candidateCards.get(random.nextInt(candidateCards.size()));

        CandidateCardsWithMatches.clear();


        //create variables to store information about how helpful a candidate card might be to create a future meld
        float rank_matches = 0;
        float suit_matches = 0;
        float seq_matches = 0;
        float total_matches = 0;


        //for each candidate card assess how helpdul it would be to create a future meld
        for (Card cands : candidateCards) {
            rank_matches = 0;
            suit_matches = 0;
            seq_matches = 0;

            for (Card handcard : cards) {

                if (handcard.rank == cands.rank)
                    rank_matches += 2; //same rank (helps with sets)
                if (handcard.suit == cands.suit) {
                    suit_matches += 2;
                    if ((Math.abs((handcard.rank - cands.rank)) <= 2) && (Math.abs((handcard.rank - cands.rank)) >= 1))
                        seq_matches += 1.4; //same suit and within 2 ranks (helps with runs) but worth only half as much
                }
            }
            //combine relevant parameters into one aggregate
            total_matches = rank_matches + seq_matches;

            //store key value pairs into map
            CandidateCardsWithMatches.put(cands, total_matches);


            //  if (turns > (new Random().nextInt(2))) {
            // if a candidate key is an edge card, set it to discard for now, will be overrided later if needed
            //   if (edges.contains(Card.rankNames[cands.rank]) && middle == true)
            //     discard = cands;
            // }

        }

        //      ------------This did NOT work. Please ignore.--------
        //if (high.contains(Card.rankNames[cands.rank]) && lowdraw == true)
        //discard = cands;
        //        ------------This did NOT work. Please ignore.--------


        // if(turns>(new Random().nextInt(2))){
        //if the card currently set to discard is a middle card, try to find another card to discard if possible
        //   if (middles.contains(Card.rankNames[discard.rank]))
        //   discard = candidateCards.get(random.nextInt(candidateCards.size()));

        // }

        //if the card currently set to discard is a low card, try to find another card to discard if possible
        // if(low.contains(Card.rankNames[discard.rank]))
        //  discard = candidateCards.get(random.nextInt(candidateCards.size()));


//find the card that is least likely to form a future meld . . .
        Map.Entry<Card, Float> minEntry = null;
        for (Map.Entry<Card, Float> entry : CandidateCardsWithMatches.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }

        // . . . and discard it
        for (Card candids : candidateCards) {
            if ((CandidateCardsWithMatches.get(candids)) == minEntry.getValue()) {
                discard = candids;
            }


        }


        // Prevent future repeat of draw, discard pair.
        ArrayList<Card> drawDiscard = new ArrayList<Card>();
        drawDiscard.add(drawnCard);
        drawDiscard.add(discard);
        drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
        turns++;
        return discard;
    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        // Ignore other player discards.  Remove from cards if playerNum is this player.
        if (playerNum == this.playerNum)
            cards.remove(discardedCard);


        if (playerNum != this.playerNum)
            otherCards.remove((discardedCard));

        if (!(seen.contains(discardedCard)))
            seen.add(discardedCard);


        //end of round, start new round
        round++;

        //add state to log
        gameLog.add(new State(count, cards, otherCards, seen));
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {
        // Check if deadwood of maximal meld is low enough to go out.

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
        if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > 0)) // Plays aggressively
            // and always goes for gin
            return null;
        return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
    }

    @Override
    public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
        // Melds ignored by simple player, but could affect which melds to make for complex player.
        if (playerNum != this.playerNum)
            opponentKnocked = true;
    }

    @Override
    public void reportScores(int[] scores) {
        // Ignored by simple player, but could affect strategy of more complex player.

    }

    @Override
    public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
        // Ignored by simple player, but could affect strategy of more complex player.

    }

    @Override
    public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
        // Ignored by simple player, but could affect strategy of more complex player.
    }


    public class State {

        private int fdcount = 51;

        private ArrayList<Card> cards;
        private ArrayList<Card> seen;
        private ArrayList<Card> otherCards;


        public State(int count, ArrayList<Card> cards, ArrayList<Card> othercards, ArrayList<Card> seen) {
            this.fdcount = count;
            this.otherCards = othercards;
            this.cards = cards;
            this.seen = seen;


        }


    }
}


class Helper {

    Map<Card, Float> CandidateCardsWithMatches = new HashMap<Card, Float>();
    private Random random = new Random();



    public static int getBestDeadwood(ArrayList<Card> myCards) {
        if(myCards.size() != 10) throw new IllegalArgumentException("need 10 cards");


        ArrayList<ArrayList<ArrayList<Card>>> bestMeldConfigs = GinRummyUtil.cardsToBestMeldSets(myCards);
        return bestMeldConfigs.isEmpty()?
                GinRummyUtil.getDeadwoodPoints(myCards):
                GinRummyUtil.getDeadwoodPoints(bestMeldConfigs.get(0), myCards);


    }

    public static int getBestDeadwoodAfterDiscard(ArrayList<Card> myCards) {
        if(myCards.size() != 11) throw new IllegalArgumentException("need 11 cards");

        ArrayList<Card> NewCards = (ArrayList<Card>) myCards.clone(); //will need an extra copy of hand to edit

        int bestDeadwood = 999; // set really high


        //for each card, discard it and find the deadwood of the new deck
        for(Card c: myCards) {
            NewCards.remove(c);
            bestDeadwood = Math.min(getBestDeadwood(NewCards), bestDeadwood);
            NewCards.add(c);

        }

        return bestDeadwood;

    }

    //this tells us the best card(s) to discard, in order to get the best deadwood
    public static ArrayList<Card> getBestDeadwoodCardsAfterDiscard(ArrayList<Card> myCards) {
        if(myCards.size() < 11) throw new IllegalArgumentException("need 11 cards");

        ArrayList<Card> bestDiscards = (ArrayList<Card>) myCards.clone(); // could be multiple

        ArrayList<Card> newCards = (ArrayList<Card>) myCards.clone(); // same as before, need another hand


        int bestDeadwood = getBestDeadwoodAfterDiscard(myCards); // this will tell us the best deadwood possible

        //for each card calculate deadwood leftover, if it equals best then add it to bestDiscards array
        for(Card c: myCards) {
            newCards.remove(c);
            if(getBestDeadwood(newCards) == bestDeadwood);
            bestDiscards.add(c);

            newCards.add(c);

        }

        return bestDiscards; //returns list of card(s) to discard in order to get the best deadwood amount


    }

    //tells us how many cards in deck will make gin for us
    public static int ginMakers(ArrayList<Card> deck, ArrayList<Card> myCards, ArrayList<Card> otherCards, ArrayList<Card> seen) {
        int count = 0;


        ArrayList<Card> discard = (ArrayList<Card>) myCards.clone();


        //find all cards currently in the discard pile

        for(Card d: deck){ //for each card in the deck
            if(seen.contains(d) && (!(myCards.contains(d))) && (!(otherCards.contains(d)))) //if no longer available
                discard.add(d);
        }



        //find how many cards, that are available, can enable us to make gin

        for(Card d: deck){
            if(!(myCards.contains(d)) && !(otherCards.contains(d)) && (!(discard.contains(d)))) { //check if available
                myCards.add(d);

                if(getBestDeadwoodAfterDiscard(myCards) == 0) //check if makes gin
                    count++;

                myCards.remove(d);
            }

        }

        return count;
    }

    //returns highest rank in a set of cards
    public int highest (ArrayList<Card> cards) {
        Card h = cards.get(0);
        int r = 0;

        for(Card c: cards)
            if(c.rank > r) {
                r = c.rank;
                h = c;

            }
        return r;
    }


    //finds opponent's best deadwood
    public int opponentDeadwood(ArrayList<Card> otherCards) {
        for(int i = 0; i<10; i++)
            if(otherCards.get(i) == null)
                otherCards.add(null);

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldConfigs = GinRummyUtil.cardsToBestMeldSets(otherCards);
        return bestMeldConfigs.isEmpty()?
                GinRummyUtil.getDeadwoodPoints(otherCards):
                GinRummyUtil.getDeadwoodPoints(bestMeldConfigs.get(0), otherCards);


    }


    //tells if a card will enable other to form meld
    public boolean helpsOpponentMeld(Card c, ArrayList<Card> seen, ArrayList<Card> otherCards) {

        //does it make a meld?
        boolean meld = false;

        //-1 to avoid double counting the card itself
        int rank_matches = -1;
        int seq_matches = -1;


        //if we have seen some cards
        if(!((seen.isEmpty()))) {

            //for each card we have seen assess how much does it help with the opponent with making a set
            for (int j = 0; j < seen.size(); j++) {
                if (seen.get(j).rank == c.rank) { //check if it helps other with set (same rank)
                    if ((otherCards.contains(j))) //check if the opponent has the card currently
                        rank_matches++;  //increment rank matches
                }

            }
        }


        //similarly assess how much the card helps opponent with making a run
        for (Card j : seen) {
            if ((Math.abs((j.rank - c.rank)) == 1) && (j.suit == c.suit)) //if this card helps with run
                if ((otherCards.contains(j))) //if they have this card
                    seq_matches++;

        }


        if (rank_matches >= 2 || seq_matches == 2) //if this card makes a run or set
            meld = true;


        return meld;


    }

    //tells if a card will not form a meld within 1 draw
    public ArrayList<Card> unmeldable1(ArrayList<Card> seen, ArrayList<Card> Cards, ArrayList<Card> otherCards) {

        Stack<Card> deck = Card.getShuffle(617); //get entire deck

        ArrayList<Card> thedeck = new ArrayList<Card>(deck); //store it as list

        ArrayList<Card> unmeldable = new ArrayList<Card>();

        int rank_matches = 0;
        int seq_matches = 0;

        int rank_strikes = 0;
        int seq_strikes = 0;


        for (Card c : Cards) {

            //create variables to store information about card

            //matches start at -1 to account for the card itself (avoids redundancy)
            rank_matches = -1;
            seq_matches = -1;



            rank_strikes = 0;
            seq_strikes = 0;


            //Find matches within current hand
            for (Card b : Cards) {
                if (b.rank == c.rank) //if it helps us with set
                    rank_matches++;
                if ((Math.abs((b.rank - c.rank)) == 1) && (b.suit == c.suit))
                    seq_matches++;
            }


            //find cards from deck that could have helped form a meld with this card, and if it is still drawable
            //if not count it as a "strike"

            for (Card j : thedeck) { //for every possible card in the deck
                if (j.rank == c.rank) //if it helps us with set
                    if (!(Cards.contains(j)) && (!(otherCards.contains(j))) && (seen.contains(j))) //i.e. it's out of the game, never coming back
                        rank_strikes++;

                if ((Math.abs((j.rank - c.rank)) <= 2) && (j.suit == c.suit)) //if it helps us with run
                    if (!(Cards.contains(j)) && (!(otherCards.contains(j))) && (seen.contains(j)))  //i.e. it's out of the game, never coming back
                        seq_strikes++;

            }


            //assess based on strikes and current matches, whether card is meldable within 1 draw
            //if not then add it to our results

            if ((rank_strikes >= 2) && (seq_strikes >= 3)) //never meldable, simply not enough matching cards out there that are still drawable
                unmeldable.add(c);

            if ((rank_matches == 0) && (seq_matches == 0)) //not enough current matches to be meldable in one draw
                unmeldable.add(c);



        }


        return unmeldable;
    }


    //tells if a card will not form a meld within 2 draws
    public ArrayList<Card> unmeldable2(ArrayList<Card> seen, ArrayList<Card> Cards, ArrayList<Card> otherCards) {

        Stack<Card> deck = Card.getShuffle(617);

        ArrayList<Card> thedeck = new ArrayList<Card>(deck);

        ArrayList<Card> unmeldable = new ArrayList<Card>();

        int rank_matches = 0;
        int seq_matches = 0;

        int rank_strikes = 0;
        int seq_strikes = 0;


        for (Card c : Cards) {

            //create variables to store information about card

            //matches start at -1 to account for the card itself (avoids redundancy)
            rank_matches = -1;
            seq_matches = -1;



            rank_strikes = 0;
            seq_strikes = 0;


            //Find matches within current hand
            for (Card b : Cards) {
                if (b.rank == c.rank) //if it helps us with set
                    rank_matches++;
                if ((Math.abs((b.rank - c.rank)) == 1) && (b.suit == c.suit))
                    seq_matches++;
            }


            //find cards from deck that could have helped form a meld with this card, and if it is still drawable
            //if not count it as a "strike"

            for (Card j : thedeck) { //for every possible card in the deck
                if (j.rank == c.rank) //if it helps us with set
                    if (!(Cards.contains(j)) && (!(otherCards.contains(j))) && (seen.contains(j))) //i.e. it's out of the game, never coming back
                        rank_strikes++;

                if ((Math.abs((j.rank - c.rank)) <= 2) && (j.suit == c.suit)) //if it helps us with run
                    if (!(Cards.contains(j)) && (!(otherCards.contains(j))) && (seen.contains(j)))  //i.e. it's out of the game, never coming back
                        seq_strikes++;

            }


            //assess based on strikes and current matches, whether card is meldable within 2 draws
            //if not then add it to our results

            if ((rank_strikes >= 2) && (seq_strikes >= 3)) //never meldable, simply not enough matching cards out there that are still drawable
                unmeldable.add(c);

        }

        return unmeldable;
    }


    //calculates average deadwood of a current hand based on all the currently available cards in the game
    public double avgDeadwood(ArrayList<Card> seen, ArrayList<Card> Cards, ArrayList<Card> otherCards) {
        Stack<Card> deck = Card.getShuffle(617);

        ArrayList<Card> remaining_deck = new ArrayList<Card>(deck); //get entire deck


        Card discard = Card.getCard(0);


        //remove all cards in our hand from the deck
        for (Card c : Cards) {
            remaining_deck.remove(c);
        }

        //remove all cards that are no longer in the game
        for (Card s: seen)
            if (!(Cards.contains(s)) && (!(otherCards.contains(s))))
                remaining_deck.remove(s);


        //remaining_deck now contains only cards which are out there available to us (that we do not already have)


        ArrayList<ArrayList<ArrayList<Card>>> bestMelds = GinRummyUtil.cardsToBestMeldSets(Cards);

        int total = 0; //stores total deadwood accumulated for all possibilities

        int possibilities = 0; //stores number of different possible hands after drawing a card


        for (Card r : remaining_deck) {

            //initialize all variables
            Cards = (ArrayList<Card>) Cards.clone();

            Cards.add(r); //add new card to hand
            ArrayList<Card> newhand = (ArrayList<Card>) Cards.clone(); //copy this hand

            int bestdeadwood = getBestDeadwoodAfterDiscard(newhand);

            //add this to our total
            total+=bestdeadwood;

            //increment possibilities by one
            possibilities += 1;


            //reset variables
            Cards.clear();
            newhand.clear();


        }


        return total / possibilities;


    }


    public int changeinDeadwood(Card card, ArrayList<Card> Cards, ArrayList<Card> seen, ArrayList<Card> otherCards) {


        int initial = -999; //initialize deadwood amount before drawing new card

        int after = -999;  //and after


        initial = getBestDeadwood(Cards);

        Cards.add(card);

        after = getBestDeadwoodAfterDiscard(Cards);


        return after - initial;


    }


}
