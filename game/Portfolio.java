/* $Header: /Users/blentz/rails_rcs/cvs/18xx/game/Attic/Portfolio.java,v 1.16 2005/05/21 14:15:21 evos Exp $
 *
 * Created on 09-Apr-2005 by Erik Vos
 *
 * Change Log:
 */
package game;

import java.util.*;

/**
 * @author Erik
 */
public class Portfolio
{

   /** Owned private companies */
   protected ArrayList privateCompanies = new ArrayList();

   /** Owned public company certificates */
   protected ArrayList certificates = new ArrayList();

   /** Owned public company certificates, organised in a HashMap per company */
   protected HashMap certPerCompany = new HashMap();

   /** Who owns the portfolio */
   protected CashHolder owner;

   /** Who receives the dividends (may differ from owner if that is the Bank) */
   protected boolean paysToCompany = false;

   /** Name of portfolio */
   protected String name;

   public Portfolio(String name, CashHolder holder, boolean paysToCompany)
   {
      this.name = name;
      this.owner = holder;
      this.paysToCompany = paysToCompany;
   }

   public Portfolio(String name, CashHolder holder)
   {
      this.name = name;
      this.owner = holder;
   }

   public void buyPrivate(PrivateCompanyI privateCompany, Portfolio from,
         int price)
   {

      if (from == Bank.getIpo()) {
	      Log.write(name + " buys " + privateCompany.getName() 
	              + " for " + Bank.format(price) + ".");
      } else {
	      Log.write(name + " buys " + privateCompany.getName() + " from "
	              + from.getName() + " for " + Bank.format(price) + ".");
      }

      // Move the private certificate
      from.removePrivate(privateCompany);
      this.addPrivate(privateCompany);
      privateCompany.setHolder(this);

      // Move the money
      Bank.transferCash(owner, from.owner, price);
   }

   public void buyCertificate(PublicCertificateI certificate, Portfolio from,
         int price)
   {

      // Move the certificate
      from.removeCertificate(certificate);
      this.addCertificate(certificate);
      certificate.setPortfolio(this);

      //PublicCertificate is no longer for sale.
      // Erik: this is not the intended use of available (which is now redundant).
      certificate.setAvailable(false);

      // Move the money. 
      // IPO pile doesn't hold money, so that money ought to go into the Company Treasury.
      // Erik: Sorry, but that money goes to the Bank. The Bank is the owner of the IPO.
      // The Company is capitalised when it floats, not earlier!
      //if(from.name.equalsIgnoreCase("IPO"))
      //   Bank.transferCash(owner, (PublicCompany) certificate.getCompany(), price);
      //else
      if (price != 0) Bank.transferCash(owner, from.owner, price);
   }

   //Sales of stock always go to the Bank pool
   //This method should be overridden for 1870 and other games
   //that allow price protection.
   public static void sellCertificate(PublicCertificateI certificate, Portfolio from,
         int price)
   {

      Log.write(from.getName() + " sells " + certificate.getShare() + "% of "
            + certificate.getCompany().getName() + " to the Bank for " 
            + Bank.format(price));

      // Move the certificate
      from.removeCertificate(certificate);
      Bank.getPool().addCertificate(certificate);
      certificate.setPortfolio(Bank.getPool());

      //PublicCertificate is for sale again
      certificate.setAvailable(true);

      // Move the money
      Bank.transferCash(Bank.getInstance(), from.owner, price);
   }
   
   public void transferCertificate (Certificate certificate, Portfolio to) {
       if (certificate instanceof PublicCertificateI) {
	       this.removeCertificate((PublicCertificateI)certificate);
	       to.addCertificate((PublicCertificateI)certificate);
       } else if (certificate instanceof PrivateCompanyI) {
           this.removePrivate((PrivateCompanyI)certificate);
           to.addPrivate((PrivateCompanyI)certificate);
       }
   }

   public void addPrivate(PrivateCompanyI privateCompany)
   {
      privateCompanies.add(privateCompany);
      privateCompany.setHolder(this);
   }

   public void addCertificate(PublicCertificateI certificate)
   {
      certificates.add(certificate);
      String companyName = certificate.getCompany().getName();
      if (!certPerCompany.containsKey(companyName))
      {
         certPerCompany.put(companyName, new ArrayList());
      }
      ((ArrayList) certPerCompany.get(companyName)).add(certificate);
      certificate.setPortfolio(this);
   }

   public boolean removePrivate(PrivateCompanyI privateCompany)
   {
      for (int i = 0; i < privateCompanies.size(); i++)
      {
         if (privateCompanies.get(i) == privateCompany)
         {
            privateCompanies.remove(i);
            return true;
         }
      }
      return false;
   }

   public void removeCertificate(PublicCertificateI certificate)
   {
      for (int i = 0; i < certificates.size(); i++)
      {
         if (certificates.get(i) == certificate)
         {
            certificates.remove(i);
         }
      }
      String companyName = certificate.getCompany().getName();
      ArrayList certs = (ArrayList) getCertificatesPerCompany(companyName);
      
      for (int i = 0; i < certs.size(); i++)
      {
         if (certs.get(i) == certificate)
         {
            certs.remove(i);
         }
      }

   }

   public List getPrivateCompanies()
   {
      return privateCompanies;
   }

   public List getCertificates()
   {
      return certificates;
   }

   public List getCertificatesPerCompany(String compName)
   {
      if (certPerCompany.containsKey(compName))
      {
         return (List) certPerCompany.get(compName);
      }
      else
      {
         //TODO: This is bad. If we don't find the company name
         // we should check to see if certPerCompany has been loaded
         // or possibly throw a config error.
         return new ArrayList();
      }
   }

   public PublicCertificateI getNextAvailableCertificate()
   {
      for (int i = 0; i < certificates.size(); i++)
      {
         if (((PublicCertificateI) certificates.get(i)).isAvailable())
         {
            return (PublicCertificateI) certificates.get(i);
         }
      }
      return null;
   }
   
   public PublicCertificateI findCertificate(PublicCompanyI company, boolean president) {
       return findCertificate(company, 1, president);
   }
   
   /** Find any certificate */
   public PublicCertificateI findCertificate(PublicCompanyI company, int unit, boolean president)
   {
      String companyName = company.getName();
      if (!certPerCompany.containsKey(companyName)) {
         return null;
      }
      Iterator it = ((List) certPerCompany.get(companyName)).iterator();
      PublicCertificateI cert;
      while (it.hasNext())
      {
         cert = (PublicCertificateI) it.next();
         if (cert.getCompany() == company) {
             if (president && cert.isPresidentShare()
                     || !president && !cert.isPresidentShare() && cert.getShares() == unit) {
                 return cert;
             }
         }
      }
      return null;
   }

   /**
    * @return
    */
   public CashHolder getBeneficiary(PublicCompanyI company)
   {
      if (paysToCompany)
      {
         return (CashHolder) company;
      }
      else
      {
         return owner;
      }
   }

   /**
    * @return
    */
   public CashHolder getOwner()
   {
      return owner;
   }

   /**
    * @param object
    */
   public void setOwner(CashHolder owner)
   {
      this.owner = owner;
   }

   /**
    * @return
    */
   public HashMap getCertPerCompany()
   {
      return certPerCompany;
   }

   /**
    * @return
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns percentage that a portfolio contains of one company.
    * 
    * @param company
    * @return
    */
   public int ownsShare(PublicCompanyI company)
   {
      int share = 0;
      String name = company.getName();
      if (certPerCompany.containsKey(name))
      {
         Iterator it = ((List) certPerCompany.get(name)).iterator();
         while (it.hasNext())
         {
            share += ((PublicCertificateI) it.next()).getShare();
         }
      }
      return share;
   }

   /**
    * Returns percentage that a portfolio contains of one company.
    * 
    * @param company
    * @return
    */
   public int ownsShares(PublicCompanyI company)
   {
      int shares = 0;
      String name = company.getName();
      if (certPerCompany.containsKey(name))
      {
         Iterator it = ((List) certPerCompany.get(name)).iterator();
         while (it.hasNext())
         {
            shares += ((PublicCertificateI) it.next()).getShares();
         }
      }
      return shares;
   }
   
   public int ownsCertificates (PublicCompanyI company, int unit, boolean president) {
       int certs = 0;
       String name = company.getName();
       PublicCertificateI cert;
       if (certPerCompany.containsKey(name)) {
           Iterator it = ((List) certPerCompany.get(name)).iterator();
           while (it.hasNext()) {
               cert = (PublicCertificateI)it.next();
               if (president) {
                   if (cert.isPresidentShare()) return 1;
               } else if (cert.getShares() == unit) {
                   certs++;
               }
           }
       }
       return certs;
   }
   
   /**
    * Swap this Portfolio's President certificate for common shares in another Portfolio.
    * @param company The company whose Presidency is handed over.
    * @param other The new President's portfolio.
    * @return The common certificates returned.
    */
   public List swapPresidentCertificate (PublicCompanyI company, Portfolio other) {

       List swapped = new ArrayList();
       PublicCertificateI swapCert;
       
       // Find the President's certificate
       PublicCertificateI cert = this.findCertificate(company, true);
       if (cert == null) return null;
       int shares = cert.getShares();
       
       // Check if counterparty has enough single certificates
       if (other.ownsCertificates(company, 1, false) >= shares) {
           for (int i=0; i<shares; i++) {
               swapCert = other.findCertificate(company, 1, false);
               other.transferCertificate (swapCert, this);
               swapped.add (swapCert);
               
           }
       } else if (other.ownsCertificates(company, shares, false) >= 1) {
           swapCert = other.findCertificate(company, 2, false);
           other.transferCertificate(swapCert, this);
           swapped.add (swapCert);
       } else {
           return null;
       }
       transferCertificate (cert, other);
       return swapped;
   }

}
