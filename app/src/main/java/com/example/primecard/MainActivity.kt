package com.example.primecard

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create container for cards and shuffle
    val arr = IntArray(52) {it * 1 + 1}
    val cards = arr.toMutableList()

    // variables
    var position: Int = 53     // create counter to keep the current gaming position (index of last card)
    var totalScore: Int = 0   // total score of player

    // display beginning screen
    btnAccept.text = "Begin"
    txvScore.text = "Welcome to Prime Card!!"
    updateImg(position, cards)

    // set the onClick activity of accept button
    btnAccept.setOnClickListener {
      /* Check if we need to start a new game */
      // Case 1: start a new game from restart or new
      if (btnAccept.text == "Restart" || btnAccept.text == "Begin") {
        btnAccept.text = "Accept"
        clearCheckBox()
        position = 3
        totalScore = 0
        cards.shuffle()
        txvScore.text = "Total Score: " + totalScore.toString()
        updateImg(position, cards)
      }
      // Case 2: continue current game (button is "Next", already calculated score, just move on
      else if (btnAccept.text == "Next") {
        // next is hit, move to next set of cards
        clearCheckBox()
        btnAccept.text = "Accept"
        position = checkNextCards(position, cards, totalScore)
      }
      // Case 3: should calculate score in this case
      else {
        // calculate selected card sum
        var sum: Int = getCardsSum(position, cards)

        // calculate score, update score and display Toast
        if (isPrime(sum)) {
          // update score
          totalScore += sum
          txvScore.text = "Total Score: " + totalScore.toString()

          /* Get Optimal Score */
          var optimal = getOptimalScore(position, cards)
          /* Case 1: user got optimal score:
          * display animation and move to next set of cards
          * */
          if (sum == optimal[0]) {
            // display burst of confetti in the center of screen
            burstFromCenter()
            // go to next set of cards
            Toast.makeText(this, "WOW!! You got OPTIMAL " + sum.toString() + " score!!!", Toast.LENGTH_SHORT).show()
            position = checkNextCards(position, cards, totalScore)
          }

          /* Case 2: user missed optimal score
          * should display optimal cards, mask all cards not optimal
          * and change accept button to Next (so user can take a look at optimal cards before
          * proceeding to next sets of cards after user click "Next" button
          * */
          else {
            btnAccept.text = "Next"
            maskCards(optimal[1])
            Toast.makeText(this, "You got " + sum.toString() +
                    " score\nbut these cards are better (" + optimal[0].toString() + ")",
              Toast.LENGTH_LONG).show()
          }

        }
        // sum is not prime, goto next set of cards
        else {
          Toast.makeText(this, "Oops! " + sum.toString() + " is not prime!!", Toast.LENGTH_SHORT).show()
          position = checkNextCards(position, cards, totalScore)
        }
      }
    }

    // synchronize card and checkbox behavior: click image to check the check box
    synImgCheck()

  }

  // isPrime(): check if the argument is prime or not
  private fun isPrime(sum: Int): Boolean {
    var flag = false
    for (i in 2..sum / 2) {
      // condition for non-prime number
      if (sum % i == 0) {
        flag = true
        break
      }
    }
    if (!flag)
      return true
    return false
  }

  // getScore(): translate index of a single card to score
  private fun getScore(index: Int, cards: MutableList<Int>): Int {
    // get the val of the card
    var cardVal: Int = cards[index]
    // if the index is between 0~12, group 1
    if (cardVal < 14)
      return cardVal
    if (cardVal < 27)
      return (cardVal - 13);
    if (cardVal < 40)
      return (cardVal - 26);
    return (cardVal - 39)
  }

  /* getOptimalScore(): check the optimal score of the current set of cards
  * Will return an IntArray containing optimal score (at [0]) and a bit mask that indicates the
  * combination of four cards that sum to optimal score (at [1])
  * */
  private fun getOptimalScore(p: Int, cards: MutableList<Int>): IntArray {
    var mask1 = 1    // bit mask of card 1 at cards[p]
    var mask2 = 2    // bit mask of card 2 at cards[p - 1]
    var mask3 = 4    // bit mask of card 3 at cards[p - 2]
    var mask4 = 8    // bit mask of card 4 at cards[p - 3]

    var maxMask: Int = 0
    var maxSum: Int = 0

    // Boolean to Int converter
    fun Boolean.toInt() = if (this) 1 else 0

    // 15 = bin(1111), enumerate from 1111 to 0000 to get the different combination of 4 cards
    for (i in 15 downTo 0 step 1) {
      var sum = (mask1 and i != 0).toInt() * getScore(p, cards) +
              (mask2 and i != 0).toInt() * getScore(p - 1, cards) +
              (mask3 and i != 0).toInt() * getScore(p - 2, cards) +
              (mask4 and i != 0).toInt() * getScore(p - 3, cards)
      // update maxSum if sum is prime and greater than current maxSum
      if (isPrime(sum) && sum > maxSum) {
        maxSum = sum
        maxMask = i
      }
    }
    return intArrayOf(maxSum, maxMask)
  }

  // updateImg(): will update the card view by value of position
  private fun updateImg(p: Int, cards: MutableList<Int>) {
    if (p <= 52) {
      setImgCard1(cards[p - 3])
      setImgCard2(cards[p - 2])
      setImgCard3(cards[p - 1])
      setImgCard4(cards[p])
    }
    else {
      setImgCard1(p)
      setImgCard2(p)
      setImgCard3(p)
      setImgCard4(p)
    }
  }

  // setImgCard1(): will set the image of card 1, hard coded
  private fun setImgCard1(i: Int) {
    if (i == 1) imgvCard1.setImageResource(R.drawable.c1)
    if (i == 2) imgvCard1.setImageResource(R.drawable.c2)
    if (i == 3) imgvCard1.setImageResource(R.drawable.c3)
    if (i == 4) imgvCard1.setImageResource(R.drawable.c4)
    if (i == 5) imgvCard1.setImageResource(R.drawable.c5)
    if (i == 6) imgvCard1.setImageResource(R.drawable.c6)
    if (i == 7) imgvCard1.setImageResource(R.drawable.c7)
    if (i == 8) imgvCard1.setImageResource(R.drawable.c8)
    if (i == 9) imgvCard1.setImageResource(R.drawable.c9)
    if (i == 10) imgvCard1.setImageResource(R.drawable.c10)
    if (i == 11) imgvCard1.setImageResource(R.drawable.c11)
    if (i == 12) imgvCard1.setImageResource(R.drawable.c12)
    if (i == 13) imgvCard1.setImageResource(R.drawable.c13)
    if (i == 14) imgvCard1.setImageResource(R.drawable.c14)
    if (i == 15) imgvCard1.setImageResource(R.drawable.c15)
    if (i == 16) imgvCard1.setImageResource(R.drawable.c16)
    if (i == 17) imgvCard1.setImageResource(R.drawable.c17)
    if (i == 18) imgvCard1.setImageResource(R.drawable.c18)
    if (i == 19) imgvCard1.setImageResource(R.drawable.c19)
    if (i == 20) imgvCard1.setImageResource(R.drawable.c20)
    if (i == 21) imgvCard1.setImageResource(R.drawable.c21)
    if (i == 22) imgvCard1.setImageResource(R.drawable.c22)
    if (i == 23) imgvCard1.setImageResource(R.drawable.c23)
    if (i == 24) imgvCard1.setImageResource(R.drawable.c24)
    if (i == 25) imgvCard1.setImageResource(R.drawable.c25)
    if (i == 26) imgvCard1.setImageResource(R.drawable.c26)
    if (i == 27) imgvCard1.setImageResource(R.drawable.c27)
    if (i == 28) imgvCard1.setImageResource(R.drawable.c28)
    if (i == 29) imgvCard1.setImageResource(R.drawable.c29)
    if (i == 30) imgvCard1.setImageResource(R.drawable.c30)
    if (i == 31) imgvCard1.setImageResource(R.drawable.c31)
    if (i == 32) imgvCard1.setImageResource(R.drawable.c32)
    if (i == 33) imgvCard1.setImageResource(R.drawable.c33)
    if (i == 34) imgvCard1.setImageResource(R.drawable.c34)
    if (i == 35) imgvCard1.setImageResource(R.drawable.c35)
    if (i == 36) imgvCard1.setImageResource(R.drawable.c36)
    if (i == 37) imgvCard1.setImageResource(R.drawable.c37)
    if (i == 38) imgvCard1.setImageResource(R.drawable.c38)
    if (i == 39) imgvCard1.setImageResource(R.drawable.c39)
    if (i == 40) imgvCard1.setImageResource(R.drawable.c40)
    if (i == 41) imgvCard1.setImageResource(R.drawable.c41)
    if (i == 42) imgvCard1.setImageResource(R.drawable.c42)
    if (i == 43) imgvCard1.setImageResource(R.drawable.c43)
    if (i == 44) imgvCard1.setImageResource(R.drawable.c44)
    if (i == 45) imgvCard1.setImageResource(R.drawable.c45)
    if (i == 46) imgvCard1.setImageResource(R.drawable.c46)
    if (i == 47) imgvCard1.setImageResource(R.drawable.c47)
    if (i == 48) imgvCard1.setImageResource(R.drawable.c48)
    if (i == 49) imgvCard1.setImageResource(R.drawable.c49)
    if (i == 50) imgvCard1.setImageResource(R.drawable.c50)
    if (i == 51) imgvCard1.setImageResource(R.drawable.c51)
    if (i == 52) imgvCard1.setImageResource(R.drawable.c52)
    if (i > 52) imgvCard1.setImageResource(R.drawable.red_back)
  }

  // setImgCard2(): will set the image of card 2, hard coded
  private fun setImgCard2(i: Int) {
    if (i == 1) imgvCard2.setImageResource(R.drawable.c1)
    if (i == 2) imgvCard2.setImageResource(R.drawable.c2)
    if (i == 3) imgvCard2.setImageResource(R.drawable.c3)
    if (i == 4) imgvCard2.setImageResource(R.drawable.c4)
    if (i == 5) imgvCard2.setImageResource(R.drawable.c5)
    if (i == 6) imgvCard2.setImageResource(R.drawable.c6)
    if (i == 7) imgvCard2.setImageResource(R.drawable.c7)
    if (i == 8) imgvCard2.setImageResource(R.drawable.c8)
    if (i == 9) imgvCard2.setImageResource(R.drawable.c9)
    if (i == 10) imgvCard2.setImageResource(R.drawable.c10)
    if (i == 11) imgvCard2.setImageResource(R.drawable.c11)
    if (i == 12) imgvCard2.setImageResource(R.drawable.c12)
    if (i == 13) imgvCard2.setImageResource(R.drawable.c13)
    if (i == 14) imgvCard2.setImageResource(R.drawable.c14)
    if (i == 15) imgvCard2.setImageResource(R.drawable.c15)
    if (i == 16) imgvCard2.setImageResource(R.drawable.c16)
    if (i == 17) imgvCard2.setImageResource(R.drawable.c17)
    if (i == 18) imgvCard2.setImageResource(R.drawable.c18)
    if (i == 19) imgvCard2.setImageResource(R.drawable.c19)
    if (i == 20) imgvCard2.setImageResource(R.drawable.c20)
    if (i == 21) imgvCard2.setImageResource(R.drawable.c21)
    if (i == 22) imgvCard2.setImageResource(R.drawable.c22)
    if (i == 23) imgvCard2.setImageResource(R.drawable.c23)
    if (i == 24) imgvCard2.setImageResource(R.drawable.c24)
    if (i == 25) imgvCard2.setImageResource(R.drawable.c25)
    if (i == 26) imgvCard2.setImageResource(R.drawable.c26)
    if (i == 27) imgvCard2.setImageResource(R.drawable.c27)
    if (i == 28) imgvCard2.setImageResource(R.drawable.c28)
    if (i == 29) imgvCard2.setImageResource(R.drawable.c29)
    if (i == 30) imgvCard2.setImageResource(R.drawable.c30)
    if (i == 31) imgvCard2.setImageResource(R.drawable.c31)
    if (i == 32) imgvCard2.setImageResource(R.drawable.c32)
    if (i == 33) imgvCard2.setImageResource(R.drawable.c33)
    if (i == 34) imgvCard2.setImageResource(R.drawable.c34)
    if (i == 35) imgvCard2.setImageResource(R.drawable.c35)
    if (i == 36) imgvCard2.setImageResource(R.drawable.c36)
    if (i == 37) imgvCard2.setImageResource(R.drawable.c37)
    if (i == 38) imgvCard2.setImageResource(R.drawable.c38)
    if (i == 39) imgvCard2.setImageResource(R.drawable.c39)
    if (i == 40) imgvCard2.setImageResource(R.drawable.c40)
    if (i == 41) imgvCard2.setImageResource(R.drawable.c41)
    if (i == 42) imgvCard2.setImageResource(R.drawable.c42)
    if (i == 43) imgvCard2.setImageResource(R.drawable.c43)
    if (i == 44) imgvCard2.setImageResource(R.drawable.c44)
    if (i == 45) imgvCard2.setImageResource(R.drawable.c45)
    if (i == 46) imgvCard2.setImageResource(R.drawable.c46)
    if (i == 47) imgvCard2.setImageResource(R.drawable.c47)
    if (i == 48) imgvCard2.setImageResource(R.drawable.c48)
    if (i == 49) imgvCard2.setImageResource(R.drawable.c49)
    if (i == 50) imgvCard2.setImageResource(R.drawable.c50)
    if (i == 51) imgvCard2.setImageResource(R.drawable.c51)
    if (i == 52) imgvCard2.setImageResource(R.drawable.c52)
    if (i > 52) imgvCard2.setImageResource(R.drawable.red_back)
  }

  // setImgCard3(): will set the image of card 3, hard coded
  private fun setImgCard3(i: Int) {
    if (i == 1) imgvCard3.setImageResource(R.drawable.c1)
    if (i == 2) imgvCard3.setImageResource(R.drawable.c2)
    if (i == 3) imgvCard3.setImageResource(R.drawable.c3)
    if (i == 4) imgvCard3.setImageResource(R.drawable.c4)
    if (i == 5) imgvCard3.setImageResource(R.drawable.c5)
    if (i == 6) imgvCard3.setImageResource(R.drawable.c6)
    if (i == 7) imgvCard3.setImageResource(R.drawable.c7)
    if (i == 8) imgvCard3.setImageResource(R.drawable.c8)
    if (i == 9) imgvCard3.setImageResource(R.drawable.c9)
    if (i == 10) imgvCard3.setImageResource(R.drawable.c10)
    if (i == 11) imgvCard3.setImageResource(R.drawable.c11)
    if (i == 12) imgvCard3.setImageResource(R.drawable.c12)
    if (i == 13) imgvCard3.setImageResource(R.drawable.c13)
    if (i == 14) imgvCard3.setImageResource(R.drawable.c14)
    if (i == 15) imgvCard3.setImageResource(R.drawable.c15)
    if (i == 16) imgvCard3.setImageResource(R.drawable.c16)
    if (i == 17) imgvCard3.setImageResource(R.drawable.c17)
    if (i == 18) imgvCard3.setImageResource(R.drawable.c18)
    if (i == 19) imgvCard3.setImageResource(R.drawable.c19)
    if (i == 20) imgvCard3.setImageResource(R.drawable.c20)
    if (i == 21) imgvCard3.setImageResource(R.drawable.c21)
    if (i == 22) imgvCard3.setImageResource(R.drawable.c22)
    if (i == 23) imgvCard3.setImageResource(R.drawable.c23)
    if (i == 24) imgvCard3.setImageResource(R.drawable.c24)
    if (i == 25) imgvCard3.setImageResource(R.drawable.c25)
    if (i == 26) imgvCard3.setImageResource(R.drawable.c26)
    if (i == 27) imgvCard3.setImageResource(R.drawable.c27)
    if (i == 28) imgvCard3.setImageResource(R.drawable.c28)
    if (i == 29) imgvCard3.setImageResource(R.drawable.c29)
    if (i == 30) imgvCard3.setImageResource(R.drawable.c30)
    if (i == 31) imgvCard3.setImageResource(R.drawable.c31)
    if (i == 32) imgvCard3.setImageResource(R.drawable.c32)
    if (i == 33) imgvCard3.setImageResource(R.drawable.c33)
    if (i == 34) imgvCard3.setImageResource(R.drawable.c34)
    if (i == 35) imgvCard3.setImageResource(R.drawable.c35)
    if (i == 36) imgvCard3.setImageResource(R.drawable.c36)
    if (i == 37) imgvCard3.setImageResource(R.drawable.c37)
    if (i == 38) imgvCard3.setImageResource(R.drawable.c38)
    if (i == 39) imgvCard3.setImageResource(R.drawable.c39)
    if (i == 40) imgvCard3.setImageResource(R.drawable.c40)
    if (i == 41) imgvCard3.setImageResource(R.drawable.c41)
    if (i == 42) imgvCard3.setImageResource(R.drawable.c42)
    if (i == 43) imgvCard3.setImageResource(R.drawable.c43)
    if (i == 44) imgvCard3.setImageResource(R.drawable.c44)
    if (i == 45) imgvCard3.setImageResource(R.drawable.c45)
    if (i == 46) imgvCard3.setImageResource(R.drawable.c46)
    if (i == 47) imgvCard3.setImageResource(R.drawable.c47)
    if (i == 48) imgvCard3.setImageResource(R.drawable.c48)
    if (i == 49) imgvCard3.setImageResource(R.drawable.c49)
    if (i == 50) imgvCard3.setImageResource(R.drawable.c50)
    if (i == 51) imgvCard3.setImageResource(R.drawable.c51)
    if (i == 52) imgvCard3.setImageResource(R.drawable.c52)
    if (i > 52) imgvCard3.setImageResource(R.drawable.red_back)
  }

  // setImgCard4(): will set the image of card 4, hard coded
  private fun setImgCard4(i: Int) {
    if (i == 1) imgvCard4.setImageResource(R.drawable.c1)
    if (i == 2) imgvCard4.setImageResource(R.drawable.c2)
    if (i == 3) imgvCard4.setImageResource(R.drawable.c3)
    if (i == 4) imgvCard4.setImageResource(R.drawable.c4)
    if (i == 5) imgvCard4.setImageResource(R.drawable.c5)
    if (i == 6) imgvCard4.setImageResource(R.drawable.c6)
    if (i == 7) imgvCard4.setImageResource(R.drawable.c7)
    if (i == 8) imgvCard4.setImageResource(R.drawable.c8)
    if (i == 9) imgvCard4.setImageResource(R.drawable.c9)
    if (i == 10) imgvCard4.setImageResource(R.drawable.c10)
    if (i == 11) imgvCard4.setImageResource(R.drawable.c11)
    if (i == 12) imgvCard4.setImageResource(R.drawable.c12)
    if (i == 13) imgvCard4.setImageResource(R.drawable.c13)
    if (i == 14) imgvCard4.setImageResource(R.drawable.c14)
    if (i == 15) imgvCard4.setImageResource(R.drawable.c15)
    if (i == 16) imgvCard4.setImageResource(R.drawable.c16)
    if (i == 17) imgvCard4.setImageResource(R.drawable.c17)
    if (i == 18) imgvCard4.setImageResource(R.drawable.c18)
    if (i == 19) imgvCard4.setImageResource(R.drawable.c19)
    if (i == 20) imgvCard4.setImageResource(R.drawable.c20)
    if (i == 21) imgvCard4.setImageResource(R.drawable.c21)
    if (i == 22) imgvCard4.setImageResource(R.drawable.c22)
    if (i == 23) imgvCard4.setImageResource(R.drawable.c23)
    if (i == 24) imgvCard4.setImageResource(R.drawable.c24)
    if (i == 25) imgvCard4.setImageResource(R.drawable.c25)
    if (i == 26) imgvCard4.setImageResource(R.drawable.c26)
    if (i == 27) imgvCard4.setImageResource(R.drawable.c27)
    if (i == 28) imgvCard4.setImageResource(R.drawable.c28)
    if (i == 29) imgvCard4.setImageResource(R.drawable.c29)
    if (i == 30) imgvCard4.setImageResource(R.drawable.c30)
    if (i == 31) imgvCard4.setImageResource(R.drawable.c31)
    if (i == 32) imgvCard4.setImageResource(R.drawable.c32)
    if (i == 33) imgvCard4.setImageResource(R.drawable.c33)
    if (i == 34) imgvCard4.setImageResource(R.drawable.c34)
    if (i == 35) imgvCard4.setImageResource(R.drawable.c35)
    if (i == 36) imgvCard4.setImageResource(R.drawable.c36)
    if (i == 37) imgvCard4.setImageResource(R.drawable.c37)
    if (i == 38) imgvCard4.setImageResource(R.drawable.c38)
    if (i == 39) imgvCard4.setImageResource(R.drawable.c39)
    if (i == 40) imgvCard4.setImageResource(R.drawable.c40)
    if (i == 41) imgvCard4.setImageResource(R.drawable.c41)
    if (i == 42) imgvCard4.setImageResource(R.drawable.c42)
    if (i == 43) imgvCard4.setImageResource(R.drawable.c43)
    if (i == 44) imgvCard4.setImageResource(R.drawable.c44)
    if (i == 45) imgvCard4.setImageResource(R.drawable.c45)
    if (i == 46) imgvCard4.setImageResource(R.drawable.c46)
    if (i == 47) imgvCard4.setImageResource(R.drawable.c47)
    if (i == 48) imgvCard4.setImageResource(R.drawable.c48)
    if (i == 49) imgvCard4.setImageResource(R.drawable.c49)
    if (i == 50) imgvCard4.setImageResource(R.drawable.c50)
    if (i == 51) imgvCard4.setImageResource(R.drawable.c51)
    if (i == 52) imgvCard4.setImageResource(R.drawable.c52)
    if (i > 52) imgvCard4.setImageResource(R.drawable.red_back)
  }

  // synImgCheck(): will synchronize the image and checkbox: click image to check
  private fun synImgCheck() {
    imgvCard1.setOnClickListener {
      checkBox1.isChecked = !(checkBox1.isChecked)
    }

    imgvCard2.setOnClickListener {
      checkBox2.isChecked = !(checkBox2.isChecked)
    }

    imgvCard3.setOnClickListener {
      checkBox3.isChecked = !(checkBox3.isChecked)
    }

    imgvCard4.setOnClickListener {
      checkBox4.isChecked = !(checkBox4.isChecked)
    }
  }

  // clearCheckBox(): will clear all check boxes
  private fun clearCheckBox() {
    /* clear checkbox */
    checkBox1.isChecked = false
    checkBox2.isChecked = false
    checkBox3.isChecked = false
    checkBox4.isChecked = false
  }

  // checkNextCards(): will check next set of cards: update image and return updated position
  private fun checkNextCards(position: Int, cards: MutableList<Int>, totalScore: Int): Int {
    var positionUpdate = position + 4
    updateImg(positionUpdate, cards)
    clearCheckBox()
    if (positionUpdate > 52) {
      txvScore.text = "Final Score: " + totalScore.toString()
      btnAccept.text = "Restart"
    }
    return positionUpdate
  }

  // maskCards(): will use the mask parameter to mask un-chosen cards
  private fun maskCards(mask: Int) {
    if (mask and 1 == 0) setImgCard4(53)
    if (mask and 2 == 0) setImgCard3(53)
    if (mask and 4 == 0) setImgCard2(53)
    if (mask and 8 == 0) setImgCard1(53)
  }

  // getCardsSum(): will calculate the sum of chosen cards
  private fun getCardsSum(position: Int, cards: MutableList<Int>): Int {
    var sum: Int = 0
    if (checkBox1.isChecked) sum += getScore(position - 3, cards)
    if (checkBox2.isChecked) sum += getScore(position - 2, cards)
    if (checkBox3.isChecked) sum += getScore(position - 1, cards)
    if (checkBox4.isChecked) sum += getScore(position, cards)
    return sum
  }

  // burst Konfetti!!
  private fun burstFromCenter() {
    val colors = intArrayOf(0x00fce18a, 0x00ff726d, 0x00b48def, 0x00f4306d)
    viewKonfetti.build()
      .addColors(*colors)
      .setDirection(0.0, 359.0)
      .setSpeed(2f, 7f)
      .setFadeOutEnabled(true)
      .setTimeToLive(2000L)
      .addShapes(Shape.RECT, Shape.CIRCLE)
      .addSizes(Size(12))
      .setPosition(viewKonfetti.x + viewKonfetti.width / 2, viewKonfetti.y + viewKonfetti.height / 3)
      .burst(100)
    viewKonfetti.bringToFront()
  }
}
