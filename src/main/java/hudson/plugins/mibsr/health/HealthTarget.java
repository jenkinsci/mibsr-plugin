package hudson.plugins.mibsr.health;

import hudson.model.HealthReport;
import hudson.model.Result;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 17-Mar-2008 Time: 12:44:28
 */
public abstract class HealthTarget<M extends HealthMetric<OBSERVABLE>, OBSERVABLE>
    implements Serializable
{

    private final M metric;

    private final Float healthy;

    private final Float unhealthy;

    private final Float unstable;

    @DataBoundConstructor
    public HealthTarget( M metric, String healthy, String unhealthy, String unstable )
    {
        this.metric = metric;
        this.healthy = safeParse( healthy );
        this.unhealthy = safeParse( unhealthy );
        this.unstable = safeParse( unstable );
    }

    private static Float safeParse( String value )
    {
        if ( value == null )
        {
            return null;
        }
        try
        {
            return Float.valueOf( value );
        }
        catch ( NumberFormatException e )
        {
            return null;
        }

    }

    public M getMetric()
    {
        return metric;
    }

    public Float getHealthy()
    {
        return healthy;
    }

    public Float getUnhealthy()
    {
        return unhealthy;
    }

    public Float getUnstable()
    {
        return unstable;
    }

    public HealthReport evaluateHealth( OBSERVABLE observable, String descriptionPrefix )
    {
        float result = metric.measure( observable );
        float healthy = this.healthy == null ? metric.getBest() : this.healthy;
        float unhealthy = this.unhealthy == null ? metric.getWorst() : this.unhealthy;
        return new HealthReport(
            Math.max( 0, Math.min( 100, (int) ( ( result - unhealthy ) / ( healthy - unhealthy ) * 100 ) ) ),
            Messages._HealthTarget_Description(descriptionPrefix, metric.getName(), result));
    }

    public Result evaluateStability( OBSERVABLE observable )
    {
        float result = metric.measure( observable );
        float healthy = this.healthy == null ? metric.getBest() : this.healthy;
        float unhealthy = this.unhealthy == null ? metric.getWorst() : this.unhealthy;
        if ( unstable != null )
        {
            if ( ( healthy > unhealthy && result < unstable ) || ( healthy < unhealthy && result > unstable ) )
            {
                return Result.UNSTABLE;
            }
        }
        return Result.SUCCESS;
    }
}
