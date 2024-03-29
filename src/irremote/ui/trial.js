
import unittest

from DynamicApproach import DynamicApproachTestCase
from GreedyFraction import GreedyFractionTestCase
from BruteForce import BrutForce01, BrutFraction

class TestCaseForKnapSack(unittest.TestCase):
    def test_dynamic(self):
        val = [50,100,150,200]
        wt = [8,16,32,40]
        cap = 64

        max = 350

        self.assertEqual(DynamicApproachTestCase(cap, wt, val, len(val)), max)

    def test_greedy_frac(self):
        wt = [10,20,30]
        val = [60,100,120]
        cap = 50

        max = 240

        self.assertEqual(GreedyFractionTestCase(cap, wt, val, len(val)), max)

    def test_bruteforce_01(self):
        val = [50,100,150,200]
        wt = [8,16,32,40]
        cap = 64

        max = 350

        self.assertEqual(BrutForce01(wt, val, cap, len(val)), max)

    def test_bruteforce_frac(self):
        wt = [10,20,30]
        val = [60,100,120]
        cap = 50

        max = 240

        self.assertEqual(BrutFraction(wt, val, cap, len(val)), max)

if __name__ == "__main__":
    unittest.main()



