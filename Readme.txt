Java Code (SDK 2) containing simulator for model published in 

	Rivest, F, Kalaska, J.F., Bengio, Y. (2009) Alternative time
	representation in dopamine models. Journal of Computational
	Neuroscience. doi:10.1007/s10827-009-0191-1

See http://dx.doi.org/10.1007/s10827-009-0191-1 for details.
These files were contributed by F. Rivest.

Files: A JBuilder9 project containing the simulator for the model and
	the experiment. The simulator also works fine on Java 1.2 SDK
	2.  Since the data is saved in java serialization format, a
	java converter is also provided to convert the recorded data
	into Microsoft Access.  Empty .mdb files are provided for this
	purpose.
	
Notes: The code is provided 'as is'. Comments may not be
	up-to-date. Some functionalities may not be fully implemented.
	The code does not regenerate the exact same data as in the
	paper.  Every simulation starts with random initial weights
	and every block has some stochasticity.
 
F.R. was supported by doctoral studentships from the New Emerging Team
Grant in Computational Neuroscience (CIHR) and from the Groupe de
recherche sur le système nerveux central (FRSQ).  Y.B and J.K. were
supported by the CIHR New Emerging Team Grant in Computational
Neuroscience and an infrastructure grant from the FRSQ.

(c) Rivest, F, Kalaska, J.F., Bengio, Y. Groupe de recherche sur le
système nerveaux central, Université de Montréal, 2009

This work is licensed under the Creative Commons
Attribution-Noncommercial-Share Alike 2.5 Canada License.  To view a
copy of this licence, visit
http://creativecommons.org/licenses/by-nc-sa/2.5/ca/ or send a letter
to Creative Commons, 171 Second Street, Suite 300, San Francisco,
California 94105, USA.
