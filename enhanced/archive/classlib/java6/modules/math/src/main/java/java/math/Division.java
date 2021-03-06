/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.math;

import org.apache.harmony.math.internal.nls.Messages;

/**
 * Static library that provides all operations related with division and modular
 * arithmetic to {@link BigInteger}. Some methods are provided in both mutable
 * and immutable way. There are several variants provided listed below:
 * 
 * <ul type="circle">
 * <li> <b>Division</b>
 * <ul type="circle">
 * <li>{@link BigInteger} division and remainder by {@link BigInteger}.</li>
 * <li>{@link BigInteger} division and remainder by {@code int}.</li>
 * <li><i>gcd</i> between {@link BigInteger} numbers.</li>
 * </ul>
 * </li>
 * <li> <b>Modular arithmetic </b>
 * <ul type="circle">
 * <li>Modular exponentiation between {@link BigInteger} numbers.</li>
 * <li>Modular inverse of a {@link BigInteger} numbers.</li>
 * </ul>
 * </li>
 *</ul>
 * 
 * @author Intel Middleware Product Division
 * @author Instituto Tecnologico de Cordoba
 */
class Division {

    /**
     * Divides the array 'a' by the array 'b' and gets the quotient and the
     * remainder. Implements the Knuth's division algorithm. See D. Knuth, The
     * Art of Computer Programming, vol. 2. Steps D1-D8 correspond the steps in
     * the algorithm description.
     * 
     * @param quot the quotient
     * @param quotLength the quotient's length
     * @param a the dividend
     * @param aLength the dividend's length
     * @param b the divisor
     * @param bLength the divisor's length
     * @return the remainder
     */
    static int[] divide(int quot[], int quotLength, int a[], int aLength,
            int b[], int bLength) {

        int normA[] = new int[aLength + 1]; // the normalized dividend
        // an extra byte is needed for correct shift
        int normB[] = new int[bLength + 1]; // the normalized divisor;
        int normBLength = bLength;
        /*
         * Step D1: normalize a and b and put the results to a1 and b1 the
         * normalized divisor's first digit must be >= 2^31
         */
        int divisorShift = Integer.numberOfLeadingZeros(b[bLength - 1]);
        if (divisorShift != 0) {
            BitLevel.shiftLeft(normB, b, 0, divisorShift);
            BitLevel.shiftLeft(normA, a, 0, divisorShift);
        } else {
            System.arraycopy(a, 0, normA, 0, aLength);
            System.arraycopy(b, 0, normB, 0, bLength);
        }
        int firstDivisorDigit = normB[normBLength - 1];
        // Step D2: set the quotient index
        int i = quotLength - 1;
        int j = aLength;

        while (i >= 0) {
            // Step D3: calculate a guess digit guessDigit
            int guessDigit = 0;
            if (normA[j] == firstDivisorDigit) {
                // set guessDigit to the largest unsigned int value
                guessDigit = -1;
            } else {
                long product = (((normA[j] & 0xffffffffL) << 32) + (normA[j - 1] & 0xffffffffL));
                long res = Division.divideLongByInt(product, firstDivisorDigit);
                guessDigit = (int) res; // the quotient of divideLongByInt
                int rem = (int) (res >> 32); // the remainder of
                                                // divideLongByInt
                // decrease guessDigit by 1 while leftHand > rightHand
                if (guessDigit != 0) {
                    long leftHand = 0;
                    long rightHand = 0;
                    boolean rOverflowed = false;
                    guessDigit++; // to have the proper value in the loop
                                    // below
                    do {
                        guessDigit--;
                        if (rOverflowed) {
                            break;
                        }
                        // leftHand always fits in an unsigned long
                        leftHand = (guessDigit & 0xffffffffL)
                                * (normB[normBLength - 2] & 0xffffffffL);
                        /*
                         * rightHand can overflow; in this case the loop
                         * condition will be true in the next step of the loop
                         */
                        rightHand = ((long) rem << 32)
                                + (normA[j - 2] & 0xffffffffL);
                        long longR = (rem & 0xffffffffL)
                                + (firstDivisorDigit & 0xffffffffL);
                        /*
                         * checks that longR does not fit in an unsigned int;
                         * this ensures that rightHand will overflow unsigned
                         * long in the next step
                         */
                        if (Integer.numberOfLeadingZeros((int) (longR >>> 32)) < 32) {
                            rOverflowed = true;
                        } else {
                            rem = (int) longR;
                        }
                    } while (((leftHand ^ 0x8000000000000000L) > (rightHand ^ 0x8000000000000000L)));
                }
            }
            // Step D4: multiply normB by guessDigit and subtract the production
            // from normA.
            if (guessDigit != 0) {
                int borrow = Division.multiplyAndSubtract(normA, j
                        - normBLength, normB, normBLength,
                        guessDigit & 0xffffffffL);
                // Step D5: check the borrow
                if (borrow != 0) {
                    // Step D6: compensating addition
                    guessDigit--;
                    long carry = 0;
                    for (int k = 0; k < normBLength; k++) {
                        carry += (normA[j - normBLength + k] & 0xffffffffL)
                                + (normB[k] & 0xffffffffL);
                        normA[j - normBLength + k] = (int) carry;
                        carry >>>= 32;
                    }
                }
            }
            if (quot != null) {
                quot[i] = guessDigit;
            }
            // Step D7
            j--;
            i--;
        }
        /*
         * Step D8: we got the remainder in normA. Denormalize it id needed
         */
        if (divisorShift != 0) {
            // reuse normB
            BitLevel.shiftRight(normB, normBLength, normA, 0, divisorShift);
            return normB;
        }
        System.arraycopy(normA, 0, normB, 0, bLength);
        return normA;
    }

    /**
     * Divides an array by an integer value. Implements the Knuth's division
     * algorithm. See D. Knuth, The Art of Computer Programming, vol. 2.
     * 
     * @param dest the quotient
     * @param src the dividend
     * @param srcLength the length of the dividend
     * @param divisor the divisor
     * @return remainder
     */
    static int divideArrayByInt(int dest[], int src[], final int srcLength,
            final int divisor) {

        long rem = 0;
        long bLong = divisor & 0xffffffffL;

        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (rem << 32) | (src[i] & 0xffffffffL);
            long quot;
            if (temp >= 0) {
                quot = (temp / bLong);
                rem = (temp % bLong);
            } else {
                /*
                 * make the dividend positive shifting it right by 1 bit then
                 * get the quotient an remainder and correct them properly
                 */
                long aPos = temp >>> 1;
                long bPos = divisor >>> 1;
                quot = aPos / bPos;
                rem = aPos % bPos;
                // double the remainder and add 1 if a is odd
                rem = (rem << 1) + (temp & 1);
                if ((divisor & 1) != 0) {
                    // the divisor is odd
                    if (quot <= rem) {
                        rem -= quot;
                    } else {
                        if (quot - rem <= bLong) {
                            rem += bLong - quot;
                            quot -= 1;
                        } else {
                            rem += (bLong << 1) - quot;
                            quot -= 2;
                        }
                    }
                }
            }
            dest[i] = (int) (quot & 0xffffffffL);
        }
        return (int) rem;
    }

    /**
     * Divides an array by an integer value. Implements the Knuth's division
     * algorithm. See D. Knuth, The Art of Computer Programming, vol. 2.
     * 
     * @param src the dividend
     * @param srcLength the length of the dividend
     * @param divisor the divisor
     * @return remainder
     */
    static int remainderArrayByInt(int src[], final int srcLength,
            final int divisor) {

        long result = 0;

        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (result << 32) + (src[i] & 0xffffffffL);
            long res = divideLongByInt(temp, divisor);
            result = (int) (res >> 32);
        }
        return (int) result;
    }

    /**
     * Divides a <code>BigInteger</code> by a signed <code>int</code> and
     * returns the remainder.
     * 
     * @param dividend the BigInteger to be divided. Must be non-negative.
     * @param divisor a signed int
     * @return divide % divisor
     */
    static int remainder(BigInteger dividend, int divisor) {
        return remainderArrayByInt(dividend.digits, dividend.numberLength,
                divisor);
    }

    /**
     * Divides an unsigned long a by an unsigned int b. It is supposed that the
     * most significant bit of b is set to 1, i.e. b < 0
     * 
     * @param a the dividend
     * @param b the divisor
     * @return the long value containing the unsigned integer remainder in the
     *         left half and the unsigned integer quotient in the right half
     */
    static long divideLongByInt(long a, int b) {
        long quot;
        long rem;
        long bLong = b & 0xffffffffL;

        if (a >= 0) {
            quot = (a / bLong);
            rem = (a % bLong);
        } else {
            /*
             * Make the dividend positive shifting it right by 1 bit then get
             * the quotient an remainder and correct them properly
             */
            long aPos = a >>> 1;
            long bPos = b >>> 1;
            quot = aPos / bPos;
            rem = aPos % bPos;
            // double the remainder and add 1 if a is odd
            rem = (rem << 1) + (a & 1);
            if ((b & 1) != 0) { // the divisor is odd
                if (quot <= rem) {
                    rem -= quot;
                } else {
                    if (quot - rem <= bLong) {
                        rem += bLong - quot;
                        quot -= 1;
                    } else {
                        rem += (bLong << 1) - quot;
                        quot -= 2;
                    }
                }
            }
        }
        return (rem << 32) | (quot & 0xffffffffL);
    }

    /**
     * Computes the quotient and the remainder after a division by an {@code int}
     * number.
     * 
     * @return an array of the form {@code [quotient, remainder]}.
     */
    static BigInteger[] divideAndRemainderByInteger(BigInteger val,
            int divisor, int divisorSign) {
        // res[0] is a quotient and res[1] is a remainder:
        int[] valDigits = val.digits;
        int valLen = val.numberLength;
        int valSign = val.sign;
        if (valLen == 1) {
            long a = (valDigits[0] & 0xffffffffL);
            long b = (divisor & 0xffffffffL);
            long quo = a / b;
            long rem = a % b;
            if (valSign != divisorSign) {
                quo = -quo;
            }
            if (valSign < 0) {
                rem = -rem;
            }
            return new BigInteger[] { BigInteger.valueOf(quo),
                    BigInteger.valueOf(rem) };
        }
        int quotientLength = valLen;
        int quotientSign = ((valSign == divisorSign) ? 1 : -1);
        int quotientDigits[] = new int[quotientLength];
        int remainderDigits[];
        remainderDigits = new int[] { Division.divideArrayByInt(
                quotientDigits, valDigits, valLen, divisor) };
        BigInteger result0 = new BigInteger(quotientSign, quotientLength,
                quotientDigits);
        BigInteger result1 = new BigInteger(valSign, 1, remainderDigits);
        result0.cutOffLeadingZeroes();
        result1.cutOffLeadingZeroes();
        return new BigInteger[] { result0, result1 };
    }

    /**
     * Multiplies an array by int and subtracts it from a subarray of another
     * array.
     * 
     * @param a the array to subtract from
     * @param start the start element of the subarray of a
     * @param b the array to be multiplied and subtracted
     * @param bLen the length of b
     * @param c the multiplier of b
     * @return the carry element of subtraction
     */
    static int multiplyAndSubtract(int a[], int start, int b[], int bLen, long c) {
        int i;
        int carry = 0;
        long product;
        int productInt;

        for (i = 0; i < bLen; i++) {
            product = c * (b[i] & 0xffffffffL);
            productInt = (int) product;
            productInt += carry;
            carry = (int) (product >> 32)
                    + ((productInt ^ 0x80000000) < (carry ^ 0x80000000) ? 1 : 0);
            productInt = a[start + i] - productInt;
            if ((productInt ^ 0x80000000) > (a[start + i] ^ 0x80000000)) {
                carry++;
            }
            a[start + i] = productInt;
        }
        product = (a[start + i] & 0xffffffffL)
                - (carry & 0xffffffffL);
        a[start + i] = (int) product;
        carry = (int) (product >> 32); // -1 or 0
        return carry;
    }

    /**
     * @param m a positive modulus
     * Return the greatest common divisor of op1 and op2,
     * 
     * @param op1
     *            must be greater than zero
     * @param op2
     *            must be greater than zero
     * @see BigInteger#gcd(BigInteger)
     * @return {@code GCD(op1, op2)}
     */
    static BigInteger gcdBinary(BigInteger op1, BigInteger op2) {
        // PRE: (op1 > 0) and (op2 > 0)
        
        /*
         * Divide both number the maximal possible times by 2 without rounding
                 * gcd(2*a, 2*b) = 2 * gcd(a,b)
         */
        int lsb1 = op1.getLowestSetBit();
        int lsb2 = op2.getLowestSetBit();
        int pow2Count = Math.min(lsb1, lsb2);

            BitLevel.inplaceShiftRight(op1, lsb1);
            BitLevel.inplaceShiftRight(op2, lsb2);
        
        BigInteger swap;
        // I want op2 > op1
        if (op1.compareTo(op2) == BigInteger.GREATER) {
            swap = op1;
            op1 = op2;
            op2 = swap;
        } 
        
        do { // INV: op2 >= op1 && both are odd unless op1 = 0
            
            // Optimization for small operands
            // (op2.bitLength() < 64) implies by INV (op1.bitLength() < 64)
            if (( op2.numberLength == 1 )
            || ( ( op2.numberLength == 2 ) && ( op2.digits[1] > 0 ) )) {
                op2 = BigInteger.valueOf(Division.gcdBinary(op1.longValue(),
                        op2.longValue()));
                break;
        }
            
            // Implements one step of the Euclidean algorithm
            // To reduce one operand if it's much smaller than the other one
            if (op2.numberLength > op1.numberLength * 1.2) {
                op2 = op2.remainder(op1);
                if (op2.signum() != 0) {
                    BitLevel.inplaceShiftRight(op2, op2.getLowestSetBit());
            }
            } else {
                
                // Use Knuth's algorithm of sucessive subtract and shifting
                do {
                    Elementary.inplaceSubtract(op2, op1); // both are odd
                    BitLevel.inplaceShiftRight(op2, op2.getLowestSetBit()); // op2 is even
                } while (op2.compareTo(op1) >= BigInteger.EQUALS);
            }
            // now op1 >= op2
            swap = op2;
            op2 = op1;
            op1 = swap;
        } while (op1.sign != 0);
        return op2.shiftLeft(pow2Count);
    }

    /**
     * Performs the same as {@link #gcdBinary(BigInteger, BigInteger)}, but
     * with numbers of 63 bits, represented in positives values of {@code long}
     * type.
     * 
     * @param op1
     *            a positive number
     * @param op2
     *            a positive number
     * @see #gcdBinary(BigInteger, BigInteger)
     * @return <code>GCD(op1, op2)</code>
     */
    static long gcdBinary(long op1, long op2) {
        // PRE: (op1 > 0) and (op2 > 0)
        int lsb1 = Long.numberOfTrailingZeros(op1);
        int lsb2 = Long.numberOfTrailingZeros(op2);
        int pow2Count = Math.min(lsb1, lsb2);

        if (lsb1 != 0) {
            op1 >>>= lsb1;
        }
        if (lsb2 != 0) {
            op2 >>>= lsb2;
        }
        do {
            if (op1 >= op2) {
                op1 -= op2;
                op1 >>>= Long.numberOfTrailingZeros(op1);
            } else {
                op2 -= op1;
                op2 >>>= Long.numberOfTrailingZeros(op2);
            }
        } while (op1 != 0);
        return ( op2 << pow2Count );
    }

    
    
    
    /**
     * Calculates a.modInverse(p) Based on: Savas, E; Koc, C "The Montgomery Modular
     * Inverse - Revised"
     */
    static BigInteger modInverseMontgomery(BigInteger a, BigInteger p) {

        if (a.sign == 0){
            // ZERO hasn't inverse
            // math.19: BigInteger not invertible
            throw new ArithmeticException(Messages.getString("math.19"));
            }
        
        
        if (!p.testBit(0)){
            // montgomery inverse require even modulo
            return modInverseLorencz(a, p);
        }
        
        int m = p.numberLength * 32;
        
        Object[] save = almostMonInv(a, p);
        BigInteger r = ( (BigInteger) save[0] );
        int k = ( (Integer) save[1] ).intValue();
//		assert ( k >= n && k <= m + n );
        
        long n1 = calcN(p);
        
        if (k > m) {
            r = monPro(r, BigInteger.ONE, p, n1);
            k = k - m;
//			assert k < m;
        }
        r = monPro(r, BigInteger.ONE.shiftLeft(m - k), p, n1);
        return r;
        }
    
    /**
     * Calculate the first digit of the inverse
     */
    private static long calcN(BigInteger a) {
        long m0 = a.digits[0] & 0xFFFFFFFFL;
        long n2 = 1L; // this is a'[0]
        long powerOfTwo = 2L;
        
        do {
            if (( ( m0 * n2 ) & powerOfTwo ) != 0) {
                n2 |= powerOfTwo;
        }
            powerOfTwo <<= 1;
        } while (powerOfTwo < 0x100000000L);
        n2 = -n2;
        return n2;
        }
    
    /**
     * Used for an intermediate result of the modInverse algorithm
     * @return the pair: ((BigInteger)r, (Integer)k) where r == a^(-1) * 2^k mod (module)
     */
    private static Object[] almostMonInv(BigInteger a, BigInteger module) {
        // PRE: a \in [1, p - 1]
        BigInteger u, v, r, s;
        // make copy to use inplace method
        u = module.copy();
        v = a.copy();
        
        int max = Math.max(v.numberLength, u.numberLength);
        r = new BigInteger(1, 1, new int[max + 1]);
        s = new BigInteger(1, 1, new int[max + 1]);
        s.digits[0] = 1;
        // s == 1 && v == 0
        
        int k = 0;
        
        int lsbu = u.getLowestSetBit();
        int lsbv = v.getLowestSetBit();
        int toShift;
        
        if (lsbu > lsbv) {
            BitLevel.inplaceShiftRight(u, lsbu);
            BitLevel.inplaceShiftRight(v, lsbv);
            BitLevel.inplaceShiftLeft(r, lsbv);
            k += lsbu - lsbv;
        } else {
            BitLevel.inplaceShiftRight(u, lsbu);
            BitLevel.inplaceShiftRight(v, lsbv);
            BitLevel.inplaceShiftLeft(s, lsbu);
            k += lsbv - lsbu;
            
    }

        r.sign = 1;
        while (v.signum() > 0) {
            // INV v >= 0, u >= 0, v odd, u odd (except last iteration when v is even (0))
    
            while (u.compareTo(v) > BigInteger.EQUALS) {
                Elementary.inplaceSubtract(u, v);
                toShift = u.getLowestSetBit();
                BitLevel.inplaceShiftRight(u, toShift);
                Elementary.inplaceAdd(r, s);
                BitLevel.inplaceShiftLeft(s, toShift);
                k += toShift;
                
            }
            
            while (u.compareTo(v) <= BigInteger.EQUALS) {
                Elementary.inplaceSubtract(v, u);
                if (v.signum() == 0)
                    break;
                toShift = v.getLowestSetBit();
                BitLevel.inplaceShiftRight(v, toShift);
                Elementary.inplaceAdd(s, r);
                BitLevel.inplaceShiftLeft(r, toShift);
                k += toShift;
                
            }
            
        }
        if (!u.isOne()){
            // in u is stored the gcd
            // math.19: BigInteger not invertible.
            throw new ArithmeticException(Messages.getString("math.19"));
        }
        
        if (r.compareTo(module) >= BigInteger.EQUALS) {
            Elementary.inplaceSubtract(r, module);
        }
        r = module.subtract(r);
        
        return new Object[] { r, k };
    }
    
    /**
     * @return bi == abs(2^exp)
     */
    private static boolean isPowerOfTwo(BigInteger bi, int exp) {
        boolean result = false;
        result = ( exp >> 5 == bi.numberLength - 1 )
        && ( bi.digits[bi.numberLength - 1] == 1 << ( exp & 31 ) );
        if (result) {
            for (int i = 0; result && i < bi.numberLength - 1; i++) {
                result = bi.digits[i] == 0;
            }
        }
        return result;
    }
    
    /**
     * Calculate how many iteration of Lorencz's algorithm would perform the
     * same operation
     *
     * @param bi
     * @param n
     * @return
     */
    private static int howManyIterations(BigInteger bi, int n) {
        int i = n - 1;
        if (bi.sign > 0) {
            while (!bi.testBit(i))
                i--;
            return n - 1 - i;
        } else {
            while (bi.testBit(i))
                i--;
            return n - 1 - Math.max(i, bi.getLowestSetBit());
        }
        
    }
    
    /**
     *
     * Based on "New Algorithm for Classical Modular Inverse" Róbert Lórencz.
     * LNCS 2523 (2002)
     *
     * @return a^(-1) mod m
     */
    static BigInteger modInverseLorencz(BigInteger a, BigInteger modulo) {
        // PRE: a is coprime with modulo, a < modulo
        
        int max = Math.max(a.numberLength, modulo.numberLength);
        int uDigits[] = new int[max + 1]; // enough place to make all the inplace operation
        int vDigits[] = new int[max + 1];
        System.arraycopy(modulo.digits, 0, uDigits, 0, modulo.numberLength);
        System.arraycopy(a.digits, 0, vDigits, 0, a.numberLength);
        BigInteger u = new BigInteger(modulo.sign, modulo.numberLength,
                uDigits);
        BigInteger v = new BigInteger(a.sign, a.numberLength, vDigits);
        
        BigInteger r = new BigInteger(0, 1, new int[max + 1]); // BigInteger.ZERO;
        BigInteger s = new BigInteger(1, 1, new int[max + 1]);
        s.digits[0] = 1;
        // r == 0 && s == 1, but with enough place
        
        int coefU = 0, coefV = 0;
        int n = modulo.bitLength();
        int k;
        while (!isPowerOfTwo(u, coefU) && !isPowerOfTwo(v, coefV)) {
            
            // modification of original algorithm: I calculate how many times the algorithm will enter in the same branch of if
            k = howManyIterations(u, n);
            
            if (k != 0) {
                BitLevel.inplaceShiftLeft(u, k);
                if (coefU >= coefV) {
                    BitLevel.inplaceShiftLeft(r, k);
                } else {
                    BitLevel.inplaceShiftRight(s, Math.min(coefV - coefU, k));
                    if (k - ( coefV - coefU ) > 0) {
                        BitLevel.inplaceShiftLeft(r, k - coefV + coefU);
                    }
                }
                coefU += k;
            }
            
            k = howManyIterations(v, n);
            if (k != 0) {
                BitLevel.inplaceShiftLeft(v, k);
                if (coefV >= coefU) {
                    BitLevel.inplaceShiftLeft(s, k);
                } else {
                    BitLevel.inplaceShiftRight(r, Math.min(coefU - coefV, k));
                    if (k - ( coefU - coefV ) > 0) {
                        BitLevel.inplaceShiftLeft(s, k - coefU + coefV);
                    }
                }
                coefV += k;
                
            }
            
            if (u.signum() == v.signum()) {
                if (coefU <= coefV) {
                    Elementary.completeInPlaceSubtract(u, v);
                    Elementary.completeInPlaceSubtract(r, s);
                } else {
                    Elementary.completeInPlaceSubtract(v, u);
                    Elementary.completeInPlaceSubtract(s, r);
                }
            } else {
                if (coefU <= coefV) {
                    Elementary.completeInPlaceAdd(u, v);
                    Elementary.completeInPlaceAdd(r, s);
                } else {
                    Elementary.completeInPlaceAdd(v, u);
                    Elementary.completeInPlaceAdd(s, r);
                }
            }
            if (v.signum() == 0 || u.signum() == 0){
                // math.19: BigInteger not invertible
                throw new ArithmeticException(Messages.getString("math.19"));
            }
        }
        
        if (isPowerOfTwo(v, coefV)) {
            r = s;
            if (v.signum() != u.signum())
                u = u.negate();
        }
        if (u.testBit(n)) {
            if (r.signum() < 0) {
                r = r.negate();
            } else {
                r = modulo.subtract(r);
            }
        }
        if (r.signum() < 0) {
            r = r.add(modulo);
        }
        
        return r;
    }
    
    static BigInteger squareAndMultiply(BigInteger x2, BigInteger a2, BigInteger exponent,BigInteger modulus, long n2  ){
        BigInteger res = x2;
        for (int i = exponent.bitLength() - 1; i >= 0; i--) {
            res = monPro(res,res,modulus, n2);
            if (BitLevel.testBit(exponent, i)) {
                res = monPro(res, a2, modulus, n2);
            }
        }
        return res;
    }
    
    /*Implements the Montgomery modular exponentiation based in <i>The sliding windows algorithm and the Mongomery
     *Reduction</i>.
     *@ar.org.fitc.ref "A. Menezes,P. van Oorschot, S. Vanstone - Handbook of Applied Cryptography";
     *@see #oddModPow(BigInteger, BigInteger,
     *                           BigInteger)
     */
    static BigInteger slidingWindow(BigInteger x2, BigInteger a2, BigInteger exponent,BigInteger modulus, long n2){
        // fill odd low pows of a2
        BigInteger pows[] = new BigInteger[8];
        BigInteger res = x2;
        int lowexp;
        BigInteger x3;
        int acc3;
        pows[0] = a2;
        
        x3 = monSquare(a2,modulus,n2);
        for (int i = 1; i <= 7; i++){
            pows[i] = monPro(pows[i-1],x3,modulus,n2) ;
        }
        
        for (int i = exponent.bitLength()-1; i>=0;i--){
            if( BitLevel.testBit(exponent,i) ) {
                lowexp = 1;
                acc3 = i;
                
                for(int j = Math.max(i-3,0);j <= i-1 ;j++) {
                    if (BitLevel.testBit(exponent,j)) {
                        if (j<acc3) {
                            acc3 = j;
                            lowexp = (lowexp << (i-j))^1;
                        } else {
                            lowexp = lowexp^(1<<(j-acc3));
                        }
                    }
                }
                
                for(int j = acc3; j <= i; j++) {
                    res = monSquare(res,modulus,n2);
                }
                res = monPro(pows[(lowexp-1)>>1], res, modulus,n2);
                i = acc3 ;
            }else{
                res = monSquare(res, modulus, n2) ;
            }
        }
        return res;
    }
    
    /**
     * Performs modular exponentiation using the Montgomery Reduction. It
     * requires that all parameters be positive and the modulus be odd. >
     * 
     * @see BigInteger#modPow(BigInteger, BigInteger)
     * @see #monPro(BigInteger, BigInteger, BigInteger, long)
     * @see #slidingWindow(BigInteger, BigInteger, BigInteger, BigInteger,
     *                      long)
     * @see #squareAndMultiply(BigInteger, BigInteger, BigInteger, BigInteger,
     *                      long)
     */
    static BigInteger oddModPow(BigInteger base, BigInteger exponent,
            BigInteger modulus) {
        // PRE: (base > 0), (exponent > 0), (modulus > 0) and (odd modulus)
        int k = (modulus.numberLength << 5); // r = 2^k
        // n-residue of base [base * r (mod modulus)]
        BigInteger a2 = base.shiftLeft(k).mod(modulus);
        // n-residue of base [1 * r (mod modulus)]
        BigInteger x2 = BigInteger.ZERO.setBit(k).mod(modulus);
        BigInteger res;
        // Compute (modulus[0]^(-1)) (mod 2^32) for odd modulus
        
        long m0 = modulus.digits[0] & 0xFFFFFFFFL;
        long n2 = 1L; // this is n'[0]
        long powerOfTwo = 2L;
        // compute n2
        do {
            if (((m0 * n2) & powerOfTwo) != 0) {
                n2 |= powerOfTwo;
            }
            powerOfTwo <<= 1;
        } while (powerOfTwo < 0x100000000L);
        n2 = -n2;

        if( modulus.numberLength == 1 ){
            res = squareAndMultiply(x2,a2, exponent, modulus,n2);
        } else {
            res = slidingWindow(x2, a2, exponent, modulus, n2);
        }
        
        return monPro(res, BigInteger.ONE, modulus, n2);
    }

    /**
     * Performs modular exponentiation using the Montgomery Reduction. It
     * requires that all parameters be positive and the modulus be even. Based
     * <i>The square and multiply algorithm and the Montgomery Reduction C. K.
     * Koc - Montgomery Reduction with Even Modulus</i>. The square and
     * multiply algorithm and the Montgomery Reduction.
     * 
     * @ar.org.fitc.ref "C. K. Koc - Montgomery Reduction with Even Modulus"
     * @see BigInteger#modPow(BigInteger, BigInteger)
     */
    static BigInteger evenModPow(BigInteger base, BigInteger exponent,
            BigInteger modulus) {
        // PRE: (base > 0), (exponent > 0), (modulus > 0) and (modulus even)
        // STEP 1: Obtain the factorization 'modulus'= q * 2^j.
        int j = modulus.getLowestSetBit();
        BigInteger q = modulus.shiftRight(j);

        // STEP 2: Compute x1 := base^exponent (mod q).
        BigInteger x1 = oddModPow(base, exponent, q);

        // STEP 3: Compute x2 := base^exponent (mod 2^j).
        BigInteger x2 = pow2ModPow(base, exponent, j);

        // STEP 4: Compute q^(-1) (mod 2^j) and y := (x2-x1) * q^(-1) (mod 2^j)
        BigInteger qInv = modPow2Inverse(q, j);
        BigInteger y = (x2.subtract(x1)).multiply(qInv);
        inplaceModPow2(y, j);
        if (y.sign < 0) {
            y = y.add(BigInteger.ZERO.setBit(j));
        }
        // STEP 5: Compute and return: x1 + q * y
        return x1.add(q.multiply(y));
    }

    /**
     * It requires that all parameters be positive.
     * 
     * @return {@code base<sup>exponent</sup> mod (2<sup>j</sup>)}.
     * @see BigInteger#modPow(BigInteger, BigInteger)
     */
    static BigInteger pow2ModPow(BigInteger base, BigInteger exponent, int j) {
        // PRE: (base > 0), (exponent > 0) and (j > 0)
        BigInteger res = BigInteger.ONE;
        BigInteger e = exponent.copy();
        BigInteger baseMod2toN = base.copy();
        BigInteger res2;
        /*
         * If 'base' is odd then it's coprime with 2^j and phi(2^j) = 2^(j-1);
         * so we can reduce reduce the exponent (mod 2^(j-1)).
         */
        if (base.testBit(0)) {
            inplaceModPow2(e, j - 1);
        }
        inplaceModPow2(baseMod2toN, j);

        for (int i = e.bitLength() - 1; i >= 0; i--) {
            res2 = res.copy();
            inplaceModPow2(res2, j);
            res = res.multiply(res2);
            if (BitLevel.testBit(e, i)) {
                res = res.multiply(baseMod2toN);
                inplaceModPow2(res, j);
            }
        }
        inplaceModPow2(res, j);
        return res;
    }

    /** Implements the Montgomery Square of a BigInteger.
     * @see #monPro(BigInteger, BigInteger, BigInteger,
     * long)
     */
    static BigInteger monSquare(BigInteger aBig, BigInteger modulus,
            long n2){
        if(modulus.numberLength == 1){
            return monPro(aBig, aBig, modulus, n2);
        }
        //Squaring
        int [] a = aBig.digits;
        int [] n = modulus.digits;
        int s = modulus.numberLength;
        
        //Multiplying...
        int [] t = new int [(s<<1) + 1];
        long cs;
        
        int limit = Math.min(s,aBig.numberLength );
        for(int i=0; i<limit; i++){
            cs = 0;
            for (int j=i+1; j<limit; j++){
                cs += (0xFFFFFFFFL & t[i+j]) + (0xFFFFFFFFL & a[i]) * (0xFFFFFFFFL & a[j]) ;
                t[i+j] = (int) cs;
                cs >>>= 32;
            }
            
            t[i+limit] = (int) cs;
        }
        BitLevel.shiftLeft( t, t, 0, 1 );
        
        cs = 0;
        long carry = 0;
        for(int i=0, index = 0; i< s; i++, index++){
            cs += (0xFFFFFFFFL & a[i]) * (0xFFFFFFFFL & a[i]) + (t[index] & 0xFFFFFFFFL);
            t[index] = (int) cs;
            cs >>>= 32;
            index++;
            cs += t[index] & 0xFFFFFFFFL ;
            t[index] = (int)cs;
            cs >>>= 32;
        }
        
        //Reducing...
        /* t + m*n */
        int m = 0;
        int i, j;
        cs = carry = 0;
        for (i=0; i<s; i++){
            cs = 0;
            m = (int) ((t[i] & 0xFFFFFFFFL) * (n2 & 0xFFFFFFFFL));
            for(j=0; j<s; j++){
                cs = (t[i+j] & 0xFFFFFFFFL) +  (m & 0xFFFFFFFFL)  * (n[j] & 0xFFFFFFFFL) + (cs >>> 32);
                t[i+j] = (int) cs;
            }
            //Adding C to the result
            carry += (t[i+s] & 0xFFFFFFFFL) + ( (cs>>>32) & 0xFFFFFFFFL);
            t[i+s] = (int) carry;
            carry >>>=32;
        }
        
        t[s<<1] = (int) carry;
        
        /* t / r  */
        for(j=0; j<s+1; j++){
            t[j] = t[j+s];
        }
        /*step 3*/
        return finalSubtraction(t, s, s, modulus );
    }
    
    /**
     * Implements the Montgomery Product of two integers represented by
     * {@code int} arrays. The arrays are supposed in <i>little
     * endian</i> notation.
     * 
     * @param a The first factor of the product.
     * @param b The second factor of the product.
     * @param modulus The modulus of the operations. Z<sub>modulus</sub>.
     * @param n2 The digit modulus'[0].
     * @ar.org.fitc.ref "C. K. Koc - Analyzing and Comparing Montgomery
     *                  Multiplication Algorithms"
     * @see #modPowOdd(BigInteger, BigInteger, BigInteger)
     */
    static BigInteger monPro(BigInteger a, BigInteger b, BigInteger modulus,
            long n2) {
        int aFirst = a.numberLength - 1;
        int bFirst = b.numberLength - 1;
        int s = modulus.numberLength;
        int n[] = modulus.digits;

        int m;
        int i, j;
        int t[] = new int[(s << 1) + 1];
        long product;
        long C;
        long aI;

        for (i = 0; i < s; i++) {
            C = 0;
            aI = (i > aFirst) ? 0 : (a.digits[i] & 0xFFFFFFFFL);
            for (j = 0; j < s; j++) {
                product = (t[j] & 0xFFFFFFFFL) + C
                        + ((j > bFirst) ? 0 : (b.digits[j] & 0xFFFFFFFFL) * aI);
                C = product >>> 32;
                t[j] = (int) product;
            }
            product = (t[s] & 0xFFFFFFFFL) + C;
            C = product >>> 32;
            t[s] = (int) product;
            t[s + 1] = (int) C;

            m = (int) ((t[0] & 0xFFFFFFFFL) * n2);
            product = (t[0] & 0xFFFFFFFFL) + (m & 0xFFFFFFFFL)
                    * (n[0] & 0xFFFFFFFFL);
            C = (int) (product >>> 32);
            for (j = 1; j < s; j++) {
                product = (t[j] & 0xFFFFFFFFL) + (C & 0xFFFFFFFFL)
                        + (m & 0xFFFFFFFFL) * (n[j] & 0xFFFFFFFFL);
                C = product >>> 32;
                t[j - 1] = (int) product;
            }
            product = (t[s] & 0xFFFFFFFFL) + (C & 0xFFFFFFFFL);
            C = product >>> 32;
            t[s - 1] = (int) product;
            t[s] = t[s + 1] + (int) C;
        }
        
        return finalSubtraction(t, t.length-1 ,s, modulus);
        
    }
    /*Performs the final reduction of the Montgomery algorithm.
     *@see monPro(BigInteger, BigInteger, BigInteger,
     *long )
     *@see monSquare(BigInteger, BigInteger ,
     *long)
     */
    static BigInteger finalSubtraction(int t[], int tLength ,int s, BigInteger modulus){
        // skipping leading zeros
        int i;
        int n[] = modulus.digits;
        boolean lower = false;
        
        for (i = tLength; (i > 0) && (t[i] == 0); i--)
            ;

        if (i == s - 1) {
            for (; (i >= 0) && (t[i] == n[i]); i--)
                ;
            lower = (i >= 0) && (t[i] & 0xFFFFFFFFL) < (n[i] & 0xFFFFFFFFL);
        } else {
            lower = (i < s - 1);
        }
        BigInteger res = new BigInteger(1, s+1, t);
        // if (t >= n) compute (t - n)
        if (!lower) {
            Elementary.inplaceSubtract(res, modulus);
        }
        res.cutOffLeadingZeroes();
        return res;
    }

    /**
     * @param x an odd positive number.
     * @param n the exponent by which 2 is raised.
     * @return {@code x<sup>-1</sup> (mod 2<sup>n</sup>)}.
     */
    static BigInteger modPow2Inverse(BigInteger x, int n) {
        // PRE: (x > 0), (x is odd), and (n > 0)
        BigInteger y = new BigInteger(1, new int[1 << n]);
        y.numberLength = 1;
        y.digits[0] = 1;
        y.sign = 1;

        for (int i = 1; i < n; i++) {
            if (BitLevel.testBit(x.multiply(y), i)) {
                // Adding 2^i to y (setting the i-th bit)
                y.digits[i >> 5] |= (1 << (i & 31));
            }
        }
        return y;
    }

    /**
     * Performs {@code x = x mod (2<sup>n</sup>)}.
     * 
     * @param x a positive number, it will store the result.
     * @param n a positive exponent of {@code 2}.
     */
    static void inplaceModPow2(BigInteger x, int n) {
        // PRE: (x > 0) and (n >= 0)
        int fd = n >> 5;
        int leadingZeros;

        if ((x.numberLength < fd) || (x.bitLength() <= n)) {
            return;
        }
        leadingZeros = 32 - (n & 31);
        x.numberLength = fd + 1;
        x.digits[fd] &= (leadingZeros < 32) ? (-1 >>> leadingZeros) : 0;
        x.cutOffLeadingZeroes();
    }

}
