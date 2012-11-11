handlebars-maven-plugin
=======================

A maven plugin that pre-compiles HandleBars templates 
Usage:
			<plugin>
				<groupId>com.viddu.handlebars</groupId>
				<artifactId>handlebars-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<configuration>
					<inputDirectory>${basedir}/templates</inputDirectory>
					<outputDirectory>${basedir}/compiled</outputDirectory>
					<handlebarsLibrary>${basedir}/handlebars-1.0.rc.1.js</handlebarsLibrary>
				</configuration>
				<executions>
					<execution>
						<goals>
							<goal>compile</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

