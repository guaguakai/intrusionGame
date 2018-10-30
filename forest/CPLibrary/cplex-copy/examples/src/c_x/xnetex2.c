/* --------------------------------------------------------------------------
 * File: xnetex2.c
 * Version 12.6.1
 * --------------------------------------------------------------------------
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
 * Copyright IBM Corporation 1997, 2014. All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 * --------------------------------------------------------------------------
 */

/* xnetex2.c - Reading and optimizing a network problem.
              Transforming to LP to find conflict. */

/* Import the CPLEX function declarations and the C library 
   header file stdio.h with the following single include. */

#include <ilcplex/cplexx.h>

/* Import the declarations for the string functions */

#include <string.h>



int
main (int argc, char **argv)
{
   /* Declare variables and arrays for retrieving problem data and
      solution information later on. */

   int       status = 0;
   CPXENVptr env = NULL;
   CPXNETptr net = NULL;
   CPXLPptr  lp  = NULL;

   /* Check command line */

   if ( argc != 2 ) {
      fprintf (stderr, "Usage: %s <network file>\n", argv[0]);
      fprintf (stderr, "Exiting ...\n");
      goto TERMINATE;
   }

   /* Initialize the CPLEX environment */

   env = CPXXopenCPLEX (&status);

   /* If an error occurs, the status value indicates the reason for
      failure.  A call to CPXXgeterrorstring will produce the text of
      the error message.  Note that CPXXopenCPLEX produces no
      output, so the only way to see the cause of the error is to use
      CPXXgeterrorstring.  For other CPLEX routines, the errors will
      be seen if the CPXPARAM_ScreenOutput indicator is set to CPX_ON.  */

   if ( env == NULL ) {
      char  errmsg[CPXMESSAGEBUFSIZE];
      fprintf (stderr, "Could not open CPLEX environment.\n");
      CPXXgeterrorstring (env, status, errmsg);
      fprintf (stderr, "%s", errmsg);
      goto TERMINATE;
   }

   /* Turn on output to the screen */

   status = CPXXsetintparam (env, CPXPARAM_ScreenOutput, CPX_ON);
   if ( status ) {
      fprintf (stderr, 
               "Failure to turn on screen indicator, error %d.\n", status);
      goto TERMINATE;
   }

   /* Create the problem. */

   net = CPXXNETcreateprob (env, &status, "netex2");

   /* A returned pointer of NULL may mean that not enough memory
      was available or there was some other problem.  In the case of 
      failure, an error message will have been written to the error 
      channel from inside CPLEX.  In this example, the setting of
      the parameter CPXPARAM_ScreenOutput causes the error message to
      appear on stdout.  */

   if ( net == NULL ) {
      fprintf (stderr, "Failed to create network object.\n");
      goto TERMINATE;
   }

   /* Read network problem data from file
      with filename given as command line argument. */

   status = CPXXNETreadcopyprob (env, net, argv[1]);

   if ( status ) {
      fprintf (stderr, "Failed to build network problem.\n");
      goto TERMINATE;
   }


   /* Optimize the problem */

   status = CPXXNETprimopt (env, net);
   if ( status ) {
      fprintf (stderr, "Failed to optimize network.\n");
      goto TERMINATE;
   }

   printf ("Solution status = %d\n", CPXXNETgetstat (env, net));


   /* Check network solution status */

   if ( CPXXNETgetstat (env, net) == CPX_STAT_INFEASIBLE ) {

      /* Create LP object used for invoking infeasibility finder */

      lp = CPXXcreateprob (env, &status, "netex2");
      if ( lp == NULL ) {
         fprintf (stderr, "Failed to create LP object.\n");
         goto TERMINATE;
      }

      /* Copy LP representation of network problem to lp object, along
         with the current basis available in the network object. */

      status = CPXXcopynettolp (env, lp, net);
      if ( status ) {
         fprintf (stderr, "Failed to copy network as LP.\n");
         goto TERMINATE;
      }

      /* Optimize the LP with primal to create an LP solution.  This
         optimization will start from the basis previously generated by
         CPXXNETprimopt() as long as the advance indicator is switched
         on (its default).  */

      status = CPXXsetintparam (env, CPXPARAM_LPMethod, CPX_ALG_PRIMAL);
      if ( status ) {
         fprintf (stderr, 
                  "Failure to set LP method, error %d.\n", status);
         goto TERMINATE;
      }
  
      status = CPXXlpopt (env, lp);
      if ( status ) {
         fprintf (stderr, "Failed to optimize LP.\n");
         goto TERMINATE;
      }

      printf ("Solution status = %i\n", CPXXgetstat (env, lp));

      /* Find conflict and write it to a file */

      status = CPXXrefineconflict (env, lp, NULL, NULL);
      if ( status ) {
         fprintf (stderr, "Failed to find conflict\n");
         goto TERMINATE;
      }

      status = CPXXclpwrite (env, lp, "netex2.clp");
      if ( status ) {
         fprintf (stderr, "Failed to write conflict file\n");
         goto TERMINATE;
      }

      printf ("conflict written to file netex2.clp\n");
   }
   else {
      printf ("Network is feasible\n");
   }

   
TERMINATE:

   /* Free up the problem as allocated by CPXXNETcreateprob, if necessary */

   if ( net != NULL ) {
      status = CPXXNETfreeprob (env, &net);
      if ( status ) {
         fprintf (stderr, "CPXXNETfreeprob failed, error code %d.\n", status);
      }
   }

   /* Free up the problem as allocated by CPXXcreateprob, if necessary */

   if ( lp != NULL ) {
      status = CPXXfreeprob (env, &lp);
      if ( status ) {
         fprintf (stderr, "CPXXfreeprob failed, error code %d.\n", status);
      }
   }

   /* Free up the CPLEX environment, if necessary */

   if ( env != NULL ) {
      status = CPXXcloseCPLEX (&env);

      /* Note that CPXXcloseCPLEX produces no output,
         so the only way to see the cause of the error is to use
         CPXXgeterrorstring.  For other CPLEX routines, the errors will
         be seen if the CPXPARAM_ScreenOutput indicator is set to CPX_ON. */

      if ( status ) {
         char  errmsg[CPXMESSAGEBUFSIZE];
         fprintf (stderr, "Could not close CPLEX environment.\n");
         CPXXgeterrorstring (env, status, errmsg);
         fprintf (stderr, "%s", errmsg);
      }
   }
     
   return (status);

}  /* END main */


