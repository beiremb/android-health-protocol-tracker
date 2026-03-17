@file:Suppress("UseKtx")

package com.example.protocoltracker.ui.home

import android.content.Context

data class HomeQuote(
    val text: String,
    val author: String
)

object HomeQuotes {
    private const val PREFS_NAME = "home_quotes"
    private const val KEY_ORDER = "quote_order"
    private const val KEY_POSITION = "quote_position"

    private val quotes = listOf(
        HomeQuote("The reasonable man adapts himself to the world. The unreasonable man persists in trying to adapt the world to himself. Therefore all progress depends on the unreasonable man.", "George Bernard Shaw"),
        HomeQuote("Don't judge each day by the harvest you reap but by the seeds that you plant", "Robert Louis Stevenson"),
        HomeQuote("Try not to become a man of success, but a man of value", "Albert Einstein"),
        HomeQuote("Simplicity is the ultimate sophistication", "Leonardo da Vinci"),
        HomeQuote("I'm an old man who has known a great many troubles, most of which have never happened", "Mark Twain"),
        HomeQuote("Whenever you find yourself on the side of the majority, it's time to pause and reflect", "Mark Twain"),
        HomeQuote("Absorb what is useful. Discard what is not. And add what is uniquely your own.", "Bruce Lee"),
        HomeQuote("Never go to sleep without a request to your subconscious", "Thomas Edison"),
        HomeQuote("Most people get ahead during the time that others waste", "Henry Ford"),
        HomeQuote("Discontent is the first necessity for progress", "Thomas Edison"),
        HomeQuote("You can't build a reputation on what you're going to do", "Henry Ford"),
        HomeQuote("It takes a while to really understand your brand. But if you really listen to your customers, they’ll tell you who you are", "Tal Winter"),
        HomeQuote("Good entrepreneurs don't like risk. They seek to reduce risk", "Jeff Bezos"),
        HomeQuote("There is nothing so useless as doing efficiently that which should not be done at all", "Peter F. Drucker"),
        HomeQuote("Success is walking from failure to failure with no loss of enthusiasm", "Winston Churchill"),
        HomeQuote("Never give up on a dream just because of the time it will take to accomplish it. The time will pass anyway", "Earl Nightingale"),
        HomeQuote("The enemy of the \"best\" is often the \"good\"", "Stephen R. Covey"),
        HomeQuote("Knowing is not enough, we must apply. Willing is not enough, we must do", "Bruce Lee"),
        HomeQuote("Feelings of disappointment can either drown you or shape you", "Walt Disney"),
        HomeQuote("One of the best lessons you can learn in life is to master how to remain calm. Calm is a superpower", "Bruce Lee"),
        HomeQuote("Do not let what you cannot do interfere with what you can do", "John Wooden"),
        HomeQuote("Reality is negotiable. Outside of science and law, all rules can be bent or broken, and it doesn't require being unethical.", "Timothy Ferriss"),
        HomeQuote("It's not the daily increase, but daily decrease. Hack away at the unessential", "Bruce Lee"),
        HomeQuote("One does not accumulate but eliminate. The height of cultivation always runs to simplicity", "Bruce Lee"),
        HomeQuote("Cash flow and time. With these two currencies, all other things are possible. Without them, nothing is possible.", "Timothy Ferriss"),
        HomeQuote("We become what we think about", "Earl Nightingale"),
        HomeQuote("When you talk, you are only repeating what you already know; But when you listen, you may learn something new", "Dalai Lama"),
        HomeQuote("The whole problem with the world is that fools and fanatics are always so certain of themselves, but wiser people so full of doubts", "Bertrand Russell"),
        HomeQuote("The chains of habit are too light to be felt until they are too heavy to be broken", "Warren Buffett"),
        HomeQuote("If you lose, don't lose the lesson", "Dalai Lama"),
        HomeQuote("The object of life is not to be on the side of the masses, but to escape finding oneself in the ranks of the insane", "Marcus Aurelius"),
        HomeQuote("The significant problems we have cannot be solved at the same level of thinking we were when we created them", "Albert Einstein"),
        HomeQuote("We are what we repeatedly do, excellence, then, is not an act, but a habit", "Aristotle"),
        HomeQuote("If you must play, decide on three things at the start: the rules of the game, the stakes, and the quitting time", "Eastern philosophy"),
        HomeQuote("The superior man is modest in his speech, but exceeds with his actions", "Confucius"),
        HomeQuote("You cannot teach a man anything; you can only help him to find it within himself", "Galileo"),
        HomeQuote("We don't rise to the level of our expectations. We fall to the level of our training", "Epictetus"),
        HomeQuote("Chance favors only the prepared mind", "Pasteur"),
        HomeQuote("Study the science of art. Study the art of science. Develop your senses—especially learn how to see. Realize that everything connects to everything else.", "Leonardo da Vinci"),
        HomeQuote("The problem is not the problem. The problem is your attitude about the problem.", "Captain Jack Sparrow"),
        HomeQuote("Think like a man of action, act like a man of thought", "Henry Bergson"),
        HomeQuote("The great aim of education is not knowledge but action", "Herbert Spencer"),
        HomeQuote("There are two types of sufferers in this world. Those who suffer from a lack of life and those who suffer from an overabundance of life", "Waking Life"),
        HomeQuote("People are rewarded in public for what they practice for years in private", "Tony Robbins"),
        HomeQuote("You can't turn a no into a yes without a maybe in between", "House of Cards"),
        HomeQuote("A 'yes' is worth much more if it starts with a 'no'", "Suits"),
        HomeQuote("Life is a series of natural and spontaneous changes. Don't resist them; that only creates sorrow. Let reality be reality.", "Lao Tzu"),
        HomeQuote("The quality of life is a function of who you go through life with", "Peter Diamandis"),
        HomeQuote("The first step is to establish that something is possible; then probability will occur", "Elon Musk"),
        HomeQuote("The past perpetuates itself through lack of presence", "Ekhart Tolle"),
        HomeQuote("You're not the plan. You're the person who confronts the obstacle to the plan.", "Jordan Peterson"),
        HomeQuote("The secret to happiness is freedom. And the secret to freedom is courage.", "Thucydides"),
        HomeQuote("Always take your job seriously, never yourself", "Marvin Bower"),
        HomeQuote("We are what we speak -it defines us- it is our image", "Marvin Bower"),
        HomeQuote("Make every detail perfect and limit the number of details to perfect.", "Jack Dorsey"),
        HomeQuote("A lot of people confuse leverage for genius.", "David Senra"),
        HomeQuote("There is no mystery to mastery.", "Bill Walsh"),
        HomeQuote("There is no problem you cannot solve when you know your business from A to Z.", "Sam Zemurray"),
        HomeQuote("Doing too many things at once is the most common mistake in business.", "Peter F. Drucker"),
        HomeQuote("In a time of crisis, the mere evidence of activity can be enough to get things moving.", "Sam Zemurray"),
        HomeQuote("Be a yardstick of quality. Some people are not used to an environment where excellence is expected.", "Steve Jobs"),
        HomeQuote("Wisdom is prevention.", "Charlie Munger"),
        HomeQuote("All great events hang by a single thread.", "Napoleon Bonaparte"),
        HomeQuote("Belief comes before ability.", "David Senra"),
        HomeQuote("That's what great leaders do. They set the standard and everyone has to live up to that standard.", "Michael Jordan"),
        HomeQuote("When you say something, make sure you have said it. The chances of you having said it are only fair.", "E.B. White"),
        HomeQuote("Do not make the mistake of confusing your product with the device that delivers it.", "Michael Bloomberg"),
        HomeQuote("Nothing is more valuable than being well-known to the right people at the right time.", "Brent Beshore"),
        HomeQuote("Never try to solve a complicated problem without explicitly being able to state your objective, strategy, and tactics.", "Peter Attia"),
        HomeQuote("Business is change. Change equals opportunity.", "Li Liu"),
        HomeQuote("Expensive solutions to any kind of problem are usually the work of mediocrity.", "Ingvar Kamprad"),
        HomeQuote("Making mistakes is the privilege of the active.", "Ingvar Kamprad"),
        HomeQuote("People don't want to be informed, they want to feel informed.", "Roger Ailes"),
        HomeQuote("Don't feel entitled to anything you didn't sweat and struggle for.", "Marian Wright Edelman"),
        HomeQuote("There’s power in looking silly and not caring that you do.", "Amy Poehler"),
        HomeQuote("When you are content to be simply yourself and don't compare or compete, everyone will respect you.", "Lao Tzu")
    )

    fun nextQuote(context: Context): HomeQuote {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedOrder = prefs.getString(KEY_ORDER, null)
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.filter { it in quotes.indices }
            .orEmpty()

        val savedPosition = prefs.getInt(KEY_POSITION, 0)

        val hasValidOrder = savedOrder.size == quotes.size

        val order: List<Int>
        val position: Int

        if (!hasValidOrder || savedPosition >= savedOrder.size) {
            order = quotes.indices.shuffled()
            position = 0
            prefs.edit()
                .putString(KEY_ORDER, order.joinToString(","))
                .putInt(KEY_POSITION, 1)
                .apply()
        } else {
            order = savedOrder
            position = savedPosition
            prefs.edit()
                .putInt(KEY_POSITION, savedPosition + 1)
                .apply()
        }

        return quotes[order[position]]
    }
}