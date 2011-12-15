package rails.game.specific._1856;

import java.util.ArrayList;
import java.util.List;

import rails.algorithms.RevenueAdapter;
import rails.algorithms.RevenueStaticModifier;
import rails.common.parser.ConfigurationException;
import rails.game.*;
import rails.game.model.Storage;
import rails.game.model.Owner;
import rails.game.state.*;

public class PublicCompany_CGR extends PublicCompany implements RevenueStaticModifier {

    public static final String NAME = "CGR";

    /** Special rules apply before CGR has got its first permanent train */
    private BooleanState hadPermanentTrain;

    /** If no player has 2 shares, we need a separate attribute to mark the president. */
    private GenericState<Player> temporaryPresident = null;

    /* Cope with multiple 5% share sales in one turn */
    private IntegerState sharesSoldSoFar;
    private IntegerState squaresDownSoFar;

    /** Initialisation, to be called directly after instantiation (cloning) */
    @Override
    public void init(String name, CompanyTypeI type) {
        super.init(name, type);
        hadPermanentTrain = new BooleanState (this, name+"_HadPermanentTrain", false);

        // Share price is initially fixed
        canSharePriceVary.set(false);
        
    }
    
    @Override
    public void finishConfiguration(GameManager gameManager) throws ConfigurationException {
        super.finishConfiguration(gameManager);

        // add revenue modifier for the case that there is no train
        gameManager.getRevenueManager().addStaticModifier(this);
    }
    
    public boolean hadPermanentTrain() {
        return hadPermanentTrain.booleanValue();
    }

    public void setHadPermanentTrain(boolean hadPermanentTrain) {
        this.hadPermanentTrain.set(hadPermanentTrain);
        canSharePriceVary.set(true);
    }

    public boolean hasTemporaryPresident () {
        return getTemporaryPresident() != null;
    }

    public Player getTemporaryPresident() {
        if (temporaryPresident != null) {
            return (Player) temporaryPresident.get();
        } else {
            return null;
        }
    }
    
    public boolean mayBuyTrainType (Train train) {
        return !"4".equals(train.getId());
    }

    @Override
    public Player getPresident() {
        if (hasTemporaryPresident()) {
            return getTemporaryPresident();
        } else {
            return super.getPresident();
        }
    }

    public void setTemporaryPresident(Player temporaryPresident) {
        if (this.temporaryPresident == null) {
            this.temporaryPresident = new GenericState<Player> (this, "CGR_TempPres");
        }
        this.temporaryPresident.set(temporaryPresident);
    }

    @Override
    public boolean canRunTrains() {
        if (!hadPermanentTrain()) {
            return true;
        }
        return getNumberOfTrains() > 0;
    }

    public boolean runsWithBorrowedTrain () {
        return !hadPermanentTrain() && getNumberOfTrains() == 0;
    }

    /**
     * CGR share price does not move until a permanent train is bought.
     *
     * @param The revenue amount.
     */
    @Override
    public void withhold(int amount) {
        if (hasStockPrice && canSharePriceVary.booleanValue()) {
            stockMarket.withhold(this);
        }
    }

    @Override
    public void buyTrain(Train train, int price) {
        super.buyTrain (train, price);
        if (train.isPermanent()) setHadPermanentTrain(true);
    }

    public void setShareUnit (int percentage) {
        // Only allowed for CGR, the value must be 10
        if (shareUnit.intValue() == 5
                && percentage == 10) {
            shareUnit.set(percentage);
            // Drop the last 10 shares
            List<PublicCertificate>certs = new ArrayList<PublicCertificate>(certificates.view());
            int share = 0;
            Owner scrapHeap = bank.getScrapHeap();
            for (PublicCertificate cert : certs) {
                if (share >= 100) {
                    cert.moveTo(scrapHeap);
                    certificates.remove(cert);
                } else {
                    cert.setCertificateCount(1.0f);
                    share += cert.getShare();
                }
            }

            // Update all owner ShareModels (once)
            // to have the UI get the correct percentage
            // FIXME: Do we still neeed this
/*            List<Portfolio> done = new ArrayList<Portfolio>();
            Portfolio portfolio;
            for (PublicCertificate cert : certificates.view()) {
                portfolio = (Portfolio)cert.getHolder();
                if (!done.contains(portfolio)) {
                    portfolio.getShareModel(this).setShare();
                    done.add(portfolio);
                }
            }
*/
        }

    }

    @Override
    public boolean mustOwnATrain() {
        if (!hadPermanentTrain()) {
            return false;
        } else {
            return super.mustOwnATrain();
        }
    }

    @Override
    public String getExtraShareMarks () {
        return (hasTemporaryPresident() ? "T" : "");
    }

    public boolean modifyCalculator(RevenueAdapter revenueAdapter) {
        // check if the running company is the cgr
        if (revenueAdapter.getCompany() != this) return false;
         
        // add the diesel train
        if (runsWithBorrowedTrain()) {
            revenueAdapter.addTrainByString("D");
            return true;
        }
        return false;
    }

    public String prettyPrint(RevenueAdapter revenueAdapter) {
        
        return null;
    }
}
