/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Hugo Beilis
 * @author Osvaldo Demo
 * @author Jorge Rafael
 * @version 1.0
 */

package ar.org.fitc.test.integration.algorithmtests;

final class TestBlowfish
extends CipherTest
{
    private static final String NAME="Blowfish";

    private static final String[][] TEST_VALUES =
    {
        {"0000000000000000", "0000000000000000", "4EF997456198DD78"},
        {"FFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFF", "51866FD5B85ECB8A"},
        {"3000000000000000", "1000000000000001", "7D856F9A613063F2"},
        {"1111111111111111", "1111111111111111", "2466DD878B963C9D"},
        {"0123456789ABCDEF", "1111111111111111", "61F9C3802281B096"},
        {"1111111111111111", "0123456789ABCDEF", "7D0CC630AFDA1EC7"},
        {"0000000000000000", "0000000000000000", "4EF997456198DD78"},
        {"FEDCBA9876543210", "0123456789ABCDEF", "0ACEAB0FC6A0A28D"},
        {"7CA110454A1A6E57", "01A1D6D039776742", "59C68245EB05282B"},
        {"0131D9619DC1376E", "5CD54CA83DEF57DA", "B1B8CC0B250F09A0"},
        {"07A1133E4A0B2686", "0248D43806F67172", "1730E5778BEA1DA4"},
        {"3849674C2602319E", "51454B582DDF440A", "A25E7856CF2651EB"},
        {"04B915BA43FEB5B6", "42FD443059577FA2", "353882B109CE8F1A"},
        {"0113B970FD34F2CE", "059B5E0851CF143A", "48F4D0884C379918"},
        {"0170F175468FB5E6", "0756D8E0774761D2", "432193B78951FC98"},
        {"43297FAD38E373FE", "762514B829BF486A", "13F04154D69D1AE5"},
        {"07A7137045DA2A16", "3BDD119049372802", "2EEDDA93FFD39C79"},
        {"04689104C2FD3B2F", "26955F6835AF609A", "D887E0393C2DA6E3"},
        {"37D06BB516CB7546", "164D5E404F275232", "5F99D04F5B163969"},
        {"1F08260D1AC2465E", "6B056E18759F5CCA", "4A057A3B24D3977B"},
        {"584023641ABA6176", "004BD6EF09176062", "452031C1E4FADA8E"},
        {"025816164629B007", "480D39006EE762F2", "7555AE39F59B87BD"},
        {"49793EBC79B3258F", "437540C8698F3CFA", "53C55F9CB49FC019"},
        {"4FB05E1515AB73A7", "072D43A077075292", "7A8E7BFA937E89A3"},
        {"49E95D6D4CA229BF", "02FE55778117F12A", "CF9C5D7A4986ADB5"},
        {"018310DC409B26D6", "1D9D5C5018F728C2", "D1ABB290658BC778"},
        {"1C587F1C13924FEF", "305532286D6F295A", "55CB3774D13EF201"},
        {"0101010101010101", "0123456789ABCDEF", "FA34EC4847B268B2"},
        {"1F1F1F1F0E0E0E0E", "0123456789ABCDEF", "A790795108EA3CAE"},
        {"E0FEE0FEF1FEF1FE", "0123456789ABCDEF", "C39E072D9FAC631D"},
        {"0000000000000000", "FFFFFFFFFFFFFFFF", "014933E0CDAFF6E4"},
        {"FFFFFFFFFFFFFFFF", "0000000000000000", "F21E9A77B71C49BC"},
        {"0123456789ABCDEF", "0000000000000000", "245946885754369A"},
        {"FEDCBA9876543210", "FFFFFFFFFFFFFFFF", "6B5C5A9C5D9E0A5A"}
    };


    protected TestBlowfish()
    {
        super(NAME,provider);
    }


    protected void doIt()
    throws Exception
    {
        testCipher(provider,TEST_VALUES);
    }
}