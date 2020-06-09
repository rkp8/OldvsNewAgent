mport java.util.*;


/**
 * NEW AGENT
 
USES Helper Methods: changeinDeadwood, helpsopponent, avgdeadwood, etc 

* about mid-way through the getDiscard() method


No significant change in win rate compared to old agent

Helper Class and method implementations are at the bottom

 */
 
 
 
 
 
public class NEW_PSU_id_963827403_GinRummyPlayerV1 implements GinRummyPlayer {
    private int playerNum;
    @SuppressWarnings("unused")
    private int startingPlayerNum;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private Random random = new Random();
    private boolean opponentKnocked = false;
    Card faceUpCard, drawnCard;
    ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
    Stack<Card> deck = Card.getShuffle(617);
    int turns = 0;


    ArrayList<Card> entireDeck = new ArrayList<Card>(deck);

    //Create additional data structures to help with new functionalities

    //lists to reference edges and middle pieces by rank
    List<String> edges = Arrays.asList("A", "2", "3", "J", "Q", "K");
    List<String> middles = Arrays.asList("4", "5", "6", "7", "8", "9");

    List<String> highest = Arrays.asList("10", "J", "Q", "K");


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

    Helper h = new Helper();

    ArrayList<Card> allCards = new ArrayList<Card>();


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
        
        //Older Simple Player algorithm:
        
        // Return true if card would be a part of a meld, false otherwise.
        this.faceUpCard = card;
        @SuppressWarnings("unchecked")
        ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();

        ArrayList<Card> allCards = (ArrayList<Card>) cards.clone();

        newCards.add(card);
        onlyDeadwood = (ArrayList<Card>) newCards.clone();
        bestify = GinRummyUtil.cardsToBestMeldSets(newCards);

        // Discard a random card (not just drawn face up) leaving minimal deadwood points.
        int minDeadwood = Integer.MAX_VALUE;
        ArrayList<Card> candidateCards = new ArrayList<Card>();

        for (Card c : newCards) {
            ArrayList<Card> remainingCards = (ArrayList<Card>) newCards.clone();
            remainingCards.remove(card);
            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
            int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
            if (deadwood <= minDeadwood) {
                if (deadwood < minDeadwood) {
                    minDeadwood = deadwood;
                    candidateCards.clear();
                }
                candidateCards.add(c);
            }
        }


        
        boolean draw = false;

        //finds deadwood (not used):
        for (Card s : cards) {  //eliminate melded cards
            for (int i = 0; i < bestify.size(); i++) {
                for (int x = 0; x < bestify.get(i).size(); x++) {
                    // for (int z = 0; z < bestify.get(i).size(); z++) {
                    if ((bestify.get(i).get(x).contains(s)))
                        onlyDeadwood.remove(s);
                }
            }
        }

        
//if card fits into best meld return true:
        for (int i = 0; i < bestify.size(); i++) {
            for (int x = 0; x < bestify.get(i).size(); x++) {
                // for (int z = 0; z < bestify.get(i).size(); z++) {
                if ((bestify.get(i).get(x).contains(card)))
                    draw = true;
            }
        }

if(draw)
    allCards.add(card);

        return draw;
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

    
 //Game State Updates: 
     
     //add to otherCards if other player drew
        if (playerNum != this.playerNum)
            otherCards.add(drawnCard);


     
     //decrease face down card count by 1
        if (count > 2)
            count--;

     //add to seen if not already seen
        if (!(seen.contains(drawnCard)) && drawnCard!=null)
            seen.add(drawnCard);


    }

    @SuppressWarnings("unchecked")
    @Override
    public Card getDiscard() {
     
     //From Simple Player:
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
                candidateCards.add(card); //this array is used in next part
            }
        }
        Card discard = candidateCards.get(random.nextInt(candidateCards.size()));

        CandidateCardsWithMatches.clear();



     //Self-created Strategy
     
        //create variables to store information about how helpful a candidate card might be to create a future meld
        float rank_matches = 0;
        float suit_matches = 0;
        float seq_matches = 0;
        float total_matches = 0;


        //for each candidate card assess how helpful it would be to create a future meld
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
         
            //combine relevant parameters into one aggregate for how helpful the card is
            total_matches = rank_matches + seq_matches;

            //store key (card, total_matches) value pairs into map
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


//find the card that is least likely to form a future meld (i.e. has lowest total_matches) . . .
        Map.Entry<Card, Float> minEntry = null;
        for (Map.Entry<Card, Float> entry : CandidateCardsWithMatches.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }

        // . . . and discard it
        for (Card candids : candidateCards) {
            if ((CandidateCardsWithMatches.get(candids)) == minEntry.getValue()) {
                discard = candids; //set to discard
            }


        }
      /*  ArrayList<Card> good = new ArrayList<>();
        ArrayList<Card> unmeldable = (ArrayList<Card>) cards.clone();
        ArrayList<Card> unmeldable1 = new ArrayList<>();
        ArrayList<Card> unmeldable2 = new ArrayList<>();
        ArrayList<ArrayList<ArrayList<Card>>> bestMelds = GinRummyUtil.cardsToBestMeldSets(unmeldable);
        ArrayList<ArrayList<Card>> melds = new ArrayList<ArrayList<Card>>();*/


     
    //Create new array for use in Helper Methods:
ArrayList<Card> newCands = new ArrayList<Card>();

//create new array with all the best discard options
        for (Card candids : candidateCards) {
            if ((CandidateCardsWithMatches.get(candids)) == minEntry.getValue()) {
                newCands.add(candids);
            }


        }
        

        //BEGINNING OF HELPER METHODS


//finds if a card will help opponent with meld
        ArrayList<Card> helpsopponent = new ArrayList<>();

        for(Card c: allCards)
            if(h.helpsOpponentMeld(c, seen, otherCards))
                helpsopponent.add(c);


        
        
       
        ArrayList<Card> bestshortterm = new ArrayList<>();

        
//finds best short term discards
if(allCards.size() ==11) {
    bestshortterm.clear();
    bestshortterm = h.getBestDeadwoodCardsAfterDiscard(allCards);


    for (Card c : bestshortterm)
        if(newCands.contains(c) && (!(helpsopponent.contains(c))))
            discard = c;

    }
ArrayList<Card> all2 = (ArrayList<Card>) allCards.clone();


Card bestAvg = cards.get(1);


double bestavg = 999;
            for(Card c: allCards) {
                all2.remove(c);
                if (h.avgDeadwood(seen, all2, otherCards) < bestavg) {
                    bestavg = h.avgDeadwood(seen, all2, otherCards);
                    bestAvg = c;
                }
                all2.add(c);

            }

            if(newCands.contains(bestAvg))
                discard = bestAvg;




//CHANGE IN DEADWOOD used:
for(Card c : allCards){

            if ((h.changeinDeadwood(c, allCards, seen, otherCards) >= 0) && newCands.contains(c) && (Math.abs(c.rank - h.highest(newCands))<=4))
                discard = c;

        }




       /* for(Card c: allCards)
            if(h.unmeldable1(seen, cards, otherCards).contains(c))
                unmeldable1.add(c);
        for(Card c: allCards)
            if(h.unmeldable2(seen, cards, otherCards).contains(c))
                unmeldable2.add(c);
        ArrayList<Card> fset = new ArrayList<Card>();
        for( Card c :allCards)
            if(newCands.contains(c) && (unmeldable1.contains(c) || unmeldable2.contains(c)))
                fset.add(c);
*/


        /*double a1;
        double a2;
        for(Card c: newCands) {
            allCards.remove(c);
            a1 = h.avgDeadwood(seen, allCards, otherCards);
            allCards.add(c);
            for (Card j : newCands) {
                if (c.getId() != j.getId())
                    allCards.remove(j);
                a2 = h.avgDeadwood(seen, allCards, otherCards);
                allCards.add(j);
                if (a1 > a2)// && (!(h.helpsOpponentMeld(c, seen, otherCards))))
                    discard = c;
            }
        }*/





/*
        //first time with entire deck
 //make sure card is beneficial to deadwood
        for(Card c: allCards)
            if(h.changeinDeadwood(c, cards, seen, otherCards) > 0 )
                good.add(c);
        for(Card c: allCards)
            if(h.unmeldable1(seen, cards, otherCards).contains(c))
                unmeldable1.add(c);
        for(Card c: allCards)
            if(h.unmeldable2(seen, cards, otherCards).contains(c))
                unmeldable2.add(c);
//find close to highest rank and also unmeldable
        for (Card m : unmeldable)
            if ((newCands.contains(m)) && (unmeldable1.contains(m)))
                discard = m;
        for (Card m : unmeldable)
            if ((newCands.contains(m)) && (unmeldable1.contains(m)) && (unmeldable2.contains(m)))
                discard = m;
        for (Card m : unmeldable)
            if ((newCands.contains(m)) && (unmeldable1.contains(m)) && (unmeldable2.contains(m)) && good.contains(m))
                discard = m;
*/





            //again for if in newCands


//find close to highest rank and also unmeldable and in newCands
      /*  for (Card m : unmeldable)
            if (newCands.contains(m) && (unmeldable1.contains(m)) && newCands.contains(m))
                discard = m;
        for (Card m : unmeldable)
            if ((newCands.contains(m)) && (unmeldable1.contains(m)) && (unmeldable2.contains(m))  && newCands.contains(m))
                discard = m;
            //again for if it helps
//find close to highest rank and also unmeldable and in newCands
        for (Card m : unmeldable)
            if (newCands.contains(m) && (unmeldable1.contains(m)) && newCands.contains(m) && good.contains(m))
                discard = m;
        for (Card m : unmeldable)
            if ((newCands.contains(m)) && (unmeldable1.contains(m)) && (unmeldable2.contains(m))  && newCands.contains(m) && good.contains(m))
                discard = m;*/


    /*  ArrayList<Card> bad = new ArrayList<Card>();
    for(Card c : fset)
      if((c.rank >= h.highest(fset))) {
           discard = c;
      }*/





/*
        if (!(unmeldable.isEmpty()))
            discard = h.highest(unmeldable);
        if (!(unmeldable1.isEmpty()))
            discard = h.highest(unmeldable1);
        if (!(unmeldable2.isEmpty()))
            discard = h.highest(unmeldable2);
    */

    //if(turns>5)
      //  discard = h.highest(newCands);


       /* for(Card c: newCands)
            if(!(h.helpsOpponentMeld(c, seen, otherCards))) {
               discard = c;
            }
        for(Card c: newCands)
            if(c ==  (h.highest(newCands))) {
                discard = c;
            }
        for(Card c: newCands)
            if((c ==  (h.highest(newCands)))&&(!(h.helpsOpponentMeld(c, seen, otherCards)))) {
                discard = c;
            }*/

       allCards.remove(discard);
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
