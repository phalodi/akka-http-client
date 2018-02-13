package org.phalodi.queing

import scala.util.Random

class APIServices {


  def getProduct(n: Int, products: List[String]) = {
    if (n <= 3)
      Random.shuffle(products).take(n)
    else
      Random.shuffle(products).take(n) ++ products.combinations(2).flatten.toList.take(n - 3)
  }

  def getStatus(n: Int, status: List[String]) = status(n)


  def getPrice = Random.nextFloat() * 100


}
