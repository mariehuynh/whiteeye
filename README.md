# White Eye

Have you noticed a whitish discolouration of your child’s pupil in dim lighting conditions, from certain angles, or in place of a redeye in flash photos? The child might have leukocoria, the cause of which can be congenital cataract (60%), retinoblastoma (18%), retinal detachment (4.2%), persistent fetal vasculature (4.2%), and Coats disease (4.2%). Untreated retinoblastoma is universally fatal.  The incidence of this alone is 1 in 15,000 live births and leukocoria shows up in more than half the cases.  

We built an app at a weekend hackathon to help with prescreening at home.  Our metrics are based on the results of research reported in [this paper](http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0076677#s2).

This app works totally offline (except for a donation button to charity).  There are no ads and it doesn't phone home.


Disclaimer: We are engineers, not lawyers or doctors, and can not give you any medical advice whatsoever.  Please see a professional for a real diagnosis.  This is still under development and may not be accurate so it is for your entertainment only.  We're just trying to help, not make any money, so if you sue us, we will be sad and have to give you all of the $0 we were planning on making.  If you or someone you know is a lawyer and wants to help us navigate the legal squishiness in exchange for our thanks, a loaf of homemade bread, and a cut of the nonexistent profits, please contact Marie(marie@mariehuynh.com). 

## Who we are

* Marie Huynh
* Nick Fotopoulos
* Tiffany Nguyen
* Hugh Zhang

## What we did

1. Slice out the centers of the pupils in the leukocoria section of Figure 3 (Examples of Cropped Leukocoric and Non-Leukocoric Pupils) and batch process them to give each one an average color over the area.  

2. Assign each slice a value based on proximity of its average color to the area of positives as plotted in graphs conveniently given in Figure 10 (Saturation-Value Scale for Quantifying Leukocoria in Photographs of Children with Retinoblastoma.).  

3. Plotting them showed that they were fairly bimodal.  We developed a metric based on this.

4. Built an Android app that implemented our metric.

5. Tested images from Google Image Search.  

 
## Current state

We are happy to report that this app has accurately categorized images that were inconclusive to humans.  And by humans, I mean our team and concerned parents on the internet who posted photos before getting a formal diagnosis.  In that regard, this weekend project was a success.  There is still much to do, however.  


## Future work

- We need more data.  It is unclear how accurate this is and what the incidence of false positive/negative is.

- We need to be able to batch process a lot of photos, which means using something like OpenCV to select the pupil area automatically for scanning.

- We can build a service that will scan photos on social networking sites and alert people of possible cause for seeing an eye doctor.  This would be opt-in only.
 
## How you can help
Again, we are engineers, not lawyers, doctors, graphics designers, or copy editors.  Come help us make it better so that we can officially release something in app stores(for free)!


## For more information

1. Abdolvahabi A, Taylor BW, Holden RL, Shaw EV, Kentsis A, et al. (2013) Colorimetric and Longitudinal Analysis of Leukocoria in Recreational Photographs of Children with Retinoblastoma. PLoS ONE 8(10): e76677. doi:10.1371/journal.pone.0076677

2. Haider S, Qureshi W, Ali A. Leukocoria in children. J Pediatr Ophthalmol Strabismus. 2008;45(3):179–80.

3. Seregard S, Lundell G, Svedberg H, Kivelä T. Incidence of retinoblastoma from 1958 to 1998 in Northern Europe: advantages of birth cohort analysis. Ophthalmology. 2004;111(6):1228–32.

4. Abramson DH, Frank CM, Susman M, Whalen MP, Dunkel IJ, Boyd NW., 3rd Presenting signs of retinoblastoma. J Pediatr. 1998;132(3 Pt 1):505–8.

