println "Hello, World!"

enum Suit {SPADES, CLUBS, DIAMONDS, HEARTS}
enum Value {SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE}

class Card {
    Suit suit
    Value value;

    String toString() {
        "[" + value + " of " + suit + "]"
    }
}

class Deck {
    List<Card> cards

    Deck() {
        cards = []

        (Suit.SPADES .. Suit.HEARTS).each {
            suit -> (Value.SIX .. Value.ACE).each {
                value -> cards << new Card(suit: suit, value: value)
            }
        }
    }

    void shuffle() {
        Collections.shuffle cards
    }

    Card getTop() { cards.remove(0) }
}

class Player {
    List<Card> cards = []
}

Deck deck = new Deck()

println deck.cards

deck.shuffle()

println "deck=" + deck.cards

Player player1 = new Player()
Player player2 = new Player()

(1 .. 6).each {
    player1.cards << deck.getTop()
    player2.cards << deck.getTop()
}

println "player1's cards: " + player1.cards
println "player2's cards: " + player2.cards

println "deck=" + deck.cards

def trumpCard = deck.getTop();

println "trump card is " + trumpCard

def trumpSuit = trumpCard.suit

println "trump suit is " + trumpSuit

deck.cards << trumpCard

println "deck=" + deck.cards
