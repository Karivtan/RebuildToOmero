/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package nvvm.omerotest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.omero.OMEROCommand;
import net.imagej.omero.OMEROCredentials;
import net.imagej.omero.OMEROException;
import net.imagej.omero.OMEROServer;
import net.imagej.omero.OMEROService;
import net.imagej.omero.OMEROSession;
import net.imglib2.type.numeric.RealType;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;


@Plugin(type = Command.class, menuPath = "Plugins>OmeroConnectAndAttach")
public class OmeroConnectAttachData<T extends RealType<T>> extends OMEROCommand {
	@Parameter
	private LogService log;

	@Parameter
	private OMEROService omeroService;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Dataset mydataset;

	@Override
	public void run() {
		try {
			// Connect to OMERO.
			final OMEROServer server = new OMEROServer(getServer(), getPort());
			final OMEROCredentials credentials = //
				new OMEROCredentials(getUser(), getPassword());
			final OMEROSession session = omeroService.session(server, credentials);

			// Display Info
			log.info("Experimenter : " + session.getExperimenter());
			Gateway mygateway = session.getGateway();
			log.info("Serverversion : " + mygateway.getServerVersion());
			BrowseFacility browser = mygateway.getFacility(BrowseFacility.class);
			// ok, now we need to get some security info and user info
			ExperimenterData user = mygateway.getLoggedInUser();
	        List<GroupData> lgd = user.getGroups(); // get all groups you have access to
	        GroupData gd1= lgd.get(0);
	        SecurityContext ctx = new SecurityContext(gd1.getId());
	        Set<GroupData> groupset = browser.getAvailableGroups(ctx, user);
	        log.info(groupset.size());
	        groupset.forEach(it-> {
	        	log.info("groupsetarray : " + it.getId());
	        });
	        log.info("Loading data...");
			mydataset = session.downloadImage(20725);
			log.info("Data loaded.");
			// for some reason the data is not loaded when running from void main(String[] args).
			// it loads fine in the plugin though.
		}
		catch (final OMEROException | DSOutOfServiceException | ExecutionException | DSAccessException exc) {
			log.error(exc);
			exc.printStackTrace();
			//cancel("Error talking to OMERO: " + exc.getMessage());
		}
	}
	public static void main(String[] args) {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		ij.command().run(OmeroConnectAttachData.class, true);
	}
}